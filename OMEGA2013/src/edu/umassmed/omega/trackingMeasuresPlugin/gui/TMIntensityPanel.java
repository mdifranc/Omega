package edu.umassmed.omega.trackingMeasuresPlugin.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RootPaneContainer;

import org.jfree.chart.ChartPanel;

import edu.umassmed.omega.commons.constants.OmegaConstants;
import edu.umassmed.omega.commons.gui.GenericComboBox;
import edu.umassmed.omega.commons.gui.GenericPanel;
import edu.umassmed.omega.data.analysisRunElements.OmegaTrackingMeasuresRun;
import edu.umassmed.omega.data.trajectoryElements.OmegaSegment;
import edu.umassmed.omega.data.trajectoryElements.OmegaTrajectory;
import edu.umassmed.omega.trackingMeasuresPlugin.runnables.TMGraphProducer;
import edu.umassmed.omega.trackingMeasuresPlugin.runnables.TMIntesityGraphProducer;

public class TMIntensityPanel extends GenericPanel {
	private static final long serialVersionUID = 1124434645792957106L;

	public static final int OPTION_PEAK_SIGNAL = 0;
	public static final int OPTION_MEAN_SIGNAL = 1;
	public static final int OPTION_LOCAL_BACKGROUND = 2;
	public static final int OPTION_LOCAL_SNR = 3;

	public static final int OPTION_MIN = 0;
	public static final int OPTION_MEAN = 1;
	public static final int OPTION_MAX = 2;

	private final TMPluginPanel pluginPanel;

	private JPanel centerPanel;
	private GenericComboBox<String> xAxis_cmb, yAxis_cmb, graphType_cmb;
	private JTextField selection_txt;
	private JButton drawGraph_btt;

	private Map<OmegaTrajectory, List<OmegaSegment>> segmentsMap;
	private int maxT;
	private String oldXAxisSelection, oldYAxisSelection, oldGraphTypeSelection;

	private ChartPanel graphPanel;
	private final List<OmegaTrajectory> selectedTrajectories;

	private OmegaTrackingMeasuresRun selectedTrackingMeasuresRun;
	private Thread t;
	private TMIntesityGraphProducer graphProducer;

	public TMIntensityPanel(final RootPaneContainer parent,
	        final TMPluginPanel pluginPanel,
	        final Map<OmegaTrajectory, List<OmegaSegment>> segmentsMap) {
		super(parent);

		this.pluginPanel = pluginPanel;

		this.segmentsMap = segmentsMap;
		this.maxT = 0;
		this.oldXAxisSelection = null;
		this.oldYAxisSelection = null;
		this.oldGraphTypeSelection = null;

		this.selectedTrajectories = new ArrayList<>();
		this.t = null;

		this.setLayout(new BorderLayout());

		this.createAndAddWidgets();

		this.addListeners();
	}

	private void createAndAddWidgets() {
		final JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new FlowLayout());
		leftPanel.setPreferredSize(OmegaConstants.BUTTON_SIZE_LARGE);
		leftPanel.setSize(OmegaConstants.BUTTON_SIZE_LARGE);

		final JLabel yAxis_lbl = new JLabel("Select y axis elements");
		yAxis_lbl.setPreferredSize(OmegaConstants.BUTTON_SIZE_LARGE);
		yAxis_lbl.setSize(OmegaConstants.BUTTON_SIZE_LARGE);
		leftPanel.add(yAxis_lbl);
		this.yAxis_cmb = new GenericComboBox<>(this.getParentContainer());
		this.yAxis_cmb.addItem(TMConstants.GRAPH_NAME_INT_PEAK);
		this.yAxis_cmb.addItem(TMConstants.GRAPH_NAME_INT_MEAN);
		this.yAxis_cmb.addItem(TMConstants.GRAPH_NAME_INT_BG);
		this.yAxis_cmb.addItem(TMConstants.GRAPH_NAME_INT_SNR);
		this.yAxis_cmb.addItem(TMConstants.MAX_PEAK_INTENSITY);
		this.yAxis_cmb.addItem(TMConstants.AVG_PEAK_INTENSITY);
		this.yAxis_cmb.addItem(TMConstants.MIN_PEAK_INTENSITY);
		this.yAxis_cmb.addItem(TMConstants.MAX_MEAN_INTENSITY);
		this.yAxis_cmb.addItem(TMConstants.AVG_MEAN_INTENSITY);
		this.yAxis_cmb.addItem(TMConstants.MIN_MEAN_INTENSITY);
		this.yAxis_cmb.addItem(TMConstants.MAX_BACKGROUND);
		this.yAxis_cmb.addItem(TMConstants.AVG_BACKGROUND);
		this.yAxis_cmb.addItem(TMConstants.MIN_BACKGROUND);
		this.yAxis_cmb.addItem(TMConstants.MAX_SNR);
		this.yAxis_cmb.addItem(TMConstants.AVG_SNR);
		this.yAxis_cmb.addItem(TMConstants.MIN_SNR);
		this.yAxis_cmb.setPreferredSize(OmegaConstants.BUTTON_SIZE_LARGE);
		this.yAxis_cmb.setSize(OmegaConstants.BUTTON_SIZE_LARGE);
		leftPanel.add(this.yAxis_cmb);

		final JLabel xAxis_lbl = new JLabel("Select x axis elements");
		xAxis_lbl.setPreferredSize(OmegaConstants.BUTTON_SIZE_LARGE);
		xAxis_lbl.setSize(OmegaConstants.BUTTON_SIZE_LARGE);
		leftPanel.add(xAxis_lbl);
		this.xAxis_cmb = new GenericComboBox<>(this.getParentContainer());
		this.xAxis_cmb.addItem(TMConstants.GRAPH_LAB_X_TPT);
		this.xAxis_cmb.addItem(TMConstants.GRAPH_LAB_X_TRACK);
		// this.xAxis_cmb.addItem("Segments");
		this.xAxis_cmb.setPreferredSize(OmegaConstants.BUTTON_SIZE_LARGE);
		this.xAxis_cmb.setSize(OmegaConstants.BUTTON_SIZE_LARGE);
		leftPanel.add(this.xAxis_cmb);

		final JLabel selection_lbl = new JLabel(TMConstants.GRAPH_VAL_RANGE);
		selection_lbl.setToolTipText(TMConstants.GRAPH_VAL_RANGE_TT);
		selection_lbl.setPreferredSize(OmegaConstants.BUTTON_SIZE_LARGE);
		selection_lbl.setSize(OmegaConstants.BUTTON_SIZE_LARGE);
		leftPanel.add(selection_lbl);
		this.selection_txt = new JTextField();
		this.selection_txt.setPreferredSize(OmegaConstants.BUTTON_SIZE_LARGE);
		this.selection_txt.setSize(OmegaConstants.BUTTON_SIZE_LARGE);
		leftPanel.add(this.selection_txt);

		final JLabel graphType_lbl = new JLabel(TMConstants.GRAPH_TYPE);
		graphType_lbl.setPreferredSize(OmegaConstants.BUTTON_SIZE_LARGE);
		graphType_lbl.setSize(OmegaConstants.BUTTON_SIZE_LARGE);
		leftPanel.add(xAxis_lbl);
		this.graphType_cmb = new GenericComboBox<>(this.getParentContainer());
		this.graphType_cmb.addItem(TMConstants.GRAPH_TYPE_LINE);
		this.graphType_cmb.addItem(TMConstants.GRAPH_TYPE_BAR);
		this.graphType_cmb.addItem(TMConstants.GRAPH_TYPE_HIST);
		// this.xAxis_cmb.addItem("Segments");
		this.graphType_cmb.setPreferredSize(OmegaConstants.BUTTON_SIZE_LARGE);
		this.graphType_cmb.setSize(OmegaConstants.BUTTON_SIZE_LARGE);
		leftPanel.add(this.graphType_cmb);

		this.drawGraph_btt = new JButton(TMConstants.GRAPH_DRAW);
		this.drawGraph_btt.setPreferredSize(OmegaConstants.BUTTON_SIZE_LARGE);
		this.drawGraph_btt.setSize(OmegaConstants.BUTTON_SIZE_LARGE);
		leftPanel.add(this.drawGraph_btt);

		this.add(leftPanel, BorderLayout.WEST);

		this.centerPanel = new JPanel();
		this.centerPanel.setLayout(new FlowLayout());

		this.add(this.centerPanel, BorderLayout.CENTER);

		// this.handleChangeChart();
		// this.handleDrawChart();
	}

	private void addListeners() {
		this.xAxis_cmb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				TMIntensityPanel.this.handleChangeAxis();
			}
		});
		this.yAxis_cmb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				TMIntensityPanel.this.handleChangeAxis();
			}
		});
		this.graphType_cmb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				TMIntensityPanel.this.handleChangeAxis();
			}
		});
		this.drawGraph_btt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				TMIntensityPanel.this.handleDrawChart();
			}
		});
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent evt) {
				TMIntensityPanel.this.handleComponentResized();
			}
		});
	}

	private void handleComponentResized() {
		if (this.graphPanel == null)
			return;
		final int height = this.getHeight() - 20;
		final int width = this.getWidth()
		        - OmegaConstants.BUTTON_SIZE_LARGE.width - 20;
		int size = height;
		if (height > width) {
			size = width;
		}
		final Dimension graphDim = new Dimension(size, size);
		this.graphPanel.setSize(graphDim);
		this.graphPanel.setPreferredSize(graphDim);
		this.repaint();
	}

	private void handleDrawChart() {
		if (this.centerPanel.getComponentCount() > 0) {
			this.centerPanel.remove(this.graphPanel);
		}
		this.revalidate();
		this.repaint();
		final String xAxisSelection = (String) this.xAxis_cmb.getSelectedItem();
		final String yAxisSelection = (String) this.yAxis_cmb.getSelectedItem();
		final String graphTypeSelection = (String) this.graphType_cmb
		        .getSelectedItem();
		if ((this.segmentsMap == null) || this.segmentsMap.isEmpty()
		        || (xAxisSelection == null) || (yAxisSelection == null)
		        || (graphTypeSelection == null)
		        || (this.selectedTrackingMeasuresRun == null))
			return;
		this.oldYAxisSelection = yAxisSelection;
		this.oldXAxisSelection = xAxisSelection;
		this.oldGraphTypeSelection = graphTypeSelection;
		if (xAxisSelection.equals(TMConstants.GRAPH_LAB_X_TPT)) {
			this.handleDrawTimepointsChart();
		} else {
			this.handleDrawTracksChart();
		}
	}

	private void handleDrawTimepointsChart() {
		final String yAxisSelection = (String) this.yAxis_cmb.getSelectedItem();
		if (yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_PEAK)) {
			this.handleTimepointsChart(TMIntensityPanel.OPTION_PEAK_SIGNAL);
		} else if (yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_MEAN)) {
			this.handleTimepointsChart(TMIntensityPanel.OPTION_MEAN_SIGNAL);
		} else if (yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_BG)) {
			this.handleTimepointsChart(TMIntensityPanel.OPTION_LOCAL_BACKGROUND);
		} else if (yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_SNR)) {
			this.handleTimepointsChart(TMIntensityPanel.OPTION_LOCAL_SNR);
		}
	}

	private void handleDrawTracksChart() {
		final String yAxisSelection = (String) this.yAxis_cmb.getSelectedItem();
		if (yAxisSelection.equals(TMConstants.MIN_PEAK_INTENSITY)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_PEAK_SIGNAL,
			        TMIntensityPanel.OPTION_MIN);
		} else if (yAxisSelection.equals(TMConstants.AVG_PEAK_INTENSITY)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_PEAK_SIGNAL,
			        TMIntensityPanel.OPTION_MEAN);
		} else if (yAxisSelection.equals(TMConstants.MAX_PEAK_INTENSITY)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_PEAK_SIGNAL,
			        TMIntensityPanel.OPTION_MAX);
		} else if (yAxisSelection.equals(TMConstants.MIN_MEAN_INTENSITY)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_MEAN_SIGNAL,
			        TMIntensityPanel.OPTION_MIN);
		} else if (yAxisSelection.equals(TMConstants.AVG_MEAN_INTENSITY)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_MEAN_SIGNAL,
			        TMIntensityPanel.OPTION_MEAN);
		} else if (yAxisSelection.equals(TMConstants.MAX_MEAN_INTENSITY)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_MEAN_SIGNAL,
			        TMIntensityPanel.OPTION_MAX);
		} else if (yAxisSelection.equals(TMConstants.MIN_BACKGROUND)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_LOCAL_BACKGROUND,
			        TMIntensityPanel.OPTION_MIN);
		} else if (yAxisSelection.equals(TMConstants.AVG_BACKGROUND)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_LOCAL_BACKGROUND,
			        TMIntensityPanel.OPTION_MEAN);
		} else if (yAxisSelection.equals(TMConstants.MAX_BACKGROUND)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_LOCAL_BACKGROUND,
			        TMIntensityPanel.OPTION_MAX);
		} else if (yAxisSelection.equals(TMConstants.MIN_SNR)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_LOCAL_SNR,
			        TMIntensityPanel.OPTION_MIN);
		} else if (yAxisSelection.equals(TMConstants.AVG_SNR)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_LOCAL_SNR,
			        TMIntensityPanel.OPTION_MEAN);
		} else if (yAxisSelection.equals(TMConstants.MAX_SNR)) {
			this.handleTracksChart(TMIntensityPanel.OPTION_LOCAL_SNR,
			        TMIntensityPanel.OPTION_MAX);
		}
	}

	private void handleChangeAxis() {
		this.drawGraph_btt.setEnabled(false);
		final String yAxisSelection = (String) this.yAxis_cmb.getSelectedItem();
		final String xAxisSelection = (String) this.xAxis_cmb.getSelectedItem();
		final String graphTypeSelection = (String) this.graphType_cmb
		        .getSelectedItem();
		if (((this.oldYAxisSelection != null) && this.oldYAxisSelection
		        .equals(yAxisSelection))
		        && ((this.oldXAxisSelection != null) && this.oldXAxisSelection
		                .equals(xAxisSelection))
		        && ((this.oldGraphTypeSelection != null) && this.oldGraphTypeSelection
		                .equals(graphTypeSelection)))
			return;
		if (xAxisSelection.equals(TMConstants.GRAPH_LAB_X_TPT)) {
			if (!yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_PEAK)
			        && !yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_MEAN)
			        && !yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_BG)
			        && !yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_SNR))
				return;
		} else {
			if (yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_PEAK)
			        || yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_MEAN)
			        || yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_BG)
			        || yAxisSelection.equals(TMConstants.GRAPH_NAME_INT_SNR))
				return;
		}
		this.drawGraph_btt.setEnabled(true);
	}

	private void handleTracksChart(final int peakMeanBgSnrOption,
	        final int minMeanMaxOption) {
		this.pluginPanel.updateStatus("Preparing tracks graph");
		Map<OmegaTrajectory, List<OmegaSegment>> selectedSegmentsMap = null;
		if (this.selectedTrajectories.isEmpty()) {
			selectedSegmentsMap = this.segmentsMap;
		} else {
			selectedSegmentsMap = new LinkedHashMap<>();
			for (final OmegaTrajectory track : this.selectedTrajectories) {
				selectedSegmentsMap.put(track, this.segmentsMap.get(track));
			}
		}
		int graphType = TMGraphProducer.LINE_GRAPH;
		if (this.graphType_cmb.getSelectedItem().equals(
		        TMConstants.GRAPH_TYPE_BAR)) {
			graphType = TMGraphProducer.BAR_GRAPH;
		} else if (this.graphType_cmb.getSelectedItem().equals(
		        TMConstants.GRAPH_TYPE_HIST)) {
			graphType = TMGraphProducer.HISTOGRAM_GRAPH;
		}
		final TMIntesityGraphProducer graphProducer = new TMIntesityGraphProducer(
		        this, graphType, peakMeanBgSnrOption, minMeanMaxOption,
		        this.maxT, selectedSegmentsMap,
		        this.selectedTrackingMeasuresRun.getPeakSignalsResults(),
		        this.selectedTrackingMeasuresRun.getMeanSignalsResults(),
		        this.selectedTrackingMeasuresRun.getLocalBackgroundsResults(),
		        this.selectedTrackingMeasuresRun.getLocalSNRsResults());
		this.launchGraphProducerThread(graphProducer);
	}

	private void handleTimepointsChart(final int peakMeanBgSnrOption) {
		this.pluginPanel.updateStatus("Preparing timepoints graph");
		Map<OmegaTrajectory, List<OmegaSegment>> selectedSegmentsMap = null;
		if (this.selectedTrajectories.isEmpty()) {
			selectedSegmentsMap = this.segmentsMap;
		} else {
			selectedSegmentsMap = new LinkedHashMap<>();
			for (final OmegaTrajectory track : this.selectedTrajectories) {
				selectedSegmentsMap.put(track, this.segmentsMap.get(track));
			}
		}
		int graphType = TMGraphProducer.LINE_GRAPH;
		if (this.graphType_cmb.getSelectedItem().equals(
		        TMConstants.GRAPH_TYPE_BAR)) {
			graphType = TMGraphProducer.BAR_GRAPH;
		} else if (this.graphType_cmb.getSelectedItem().equals(
		        TMConstants.GRAPH_TYPE_HIST)) {
			graphType = TMGraphProducer.HISTOGRAM_GRAPH;
		}
		final TMIntesityGraphProducer graphProducer = new TMIntesityGraphProducer(
		        this, graphType, peakMeanBgSnrOption, this.maxT,
		        selectedSegmentsMap);
		this.launchGraphProducerThread(graphProducer);
	}

	private void launchGraphProducerThread(
	        final TMIntesityGraphProducer graphProducer) {
		if ((this.t != null) && this.t.isAlive()) {
			this.graphProducer.terminate();
		}
		this.t = new Thread(graphProducer);
		this.graphProducer = graphProducer;
		this.t.setName("IntensityGraphProducer");
		this.t.start();
	}

	public void setMaximumT(final int maxT) {
		this.maxT = maxT;
	}

	public void setSegmentsMap(
	        final Map<OmegaTrajectory, List<OmegaSegment>> segmentsMap) {
		this.segmentsMap = segmentsMap;
		// this.handleChangeChart();
		this.handleDrawChart();
	}

	@Override
	public void updateParentContainer(final RootPaneContainer parent) {
		super.updateParentContainer(parent);
		this.xAxis_cmb.updateParentContainer(parent);
		this.yAxis_cmb.updateParentContainer(parent);
	}

	public void setSelectedTrajectories(
	        final List<OmegaTrajectory> selectedTrajectories) {
		this.selectedTrajectories.clear();
		this.selectedTrajectories.addAll(selectedTrajectories);
		// this.handleChangeXChart();
		this.handleDrawChart();
	}

	public void updateSelectedTrackingMeasuresRun(
	        final OmegaTrackingMeasuresRun trackingMeasuresRun) {
		this.selectedTrackingMeasuresRun = trackingMeasuresRun;
	}

	public void updateStatus(final double completed, final boolean ended,
	        final ChartPanel graphPanel) {
		if (ended) {
			this.graphPanel = graphPanel;
			this.handleComponentResized();
			// this.graphPanel.updateParentContainer(this.getParentContainer());
			this.pluginPanel.updateStatus("Plugin ready");
			// this.add(this.graphPanel, BorderLayout.CENTER);
			this.centerPanel.add(this.graphPanel);
			this.drawGraph_btt.setEnabled(false);
			this.revalidate();
			this.repaint();
		} else {
			final String completedS = new BigDecimal(completed).setScale(2,
			        RoundingMode.HALF_UP).toString();
			this.pluginPanel
			        .updateStatus("Graph " + completedS + " completed.");
		}
	}
}
