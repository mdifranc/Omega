package edu.umassmed.omega.omegaDataBrowserPlugin.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.RootPaneContainer;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.umassmed.omega.commons.gui.GenericPanel;
import edu.umassmed.omega.commons.gui.checkboxTree.CheckBoxNode;
import edu.umassmed.omega.commons.gui.checkboxTree.CheckBoxNodeEditor;
import edu.umassmed.omega.commons.gui.checkboxTree.CheckBoxNodeRenderer;
import edu.umassmed.omega.commons.gui.checkboxTree.CheckBoxStatus;
import edu.umassmed.omega.dataNew.analysisRunElements.OmegaAnalysisRun;
import edu.umassmed.omega.dataNew.analysisRunElements.OmegaAnalysisRunContainer;
import edu.umassmed.omega.dataNew.coreElements.OmegaElement;

public class OmegaDataBrowserAnalysisBrowserPanel extends GenericPanel {

	private static final long serialVersionUID = 5212368402335926305L;

	private final OmegaDataBrowserPluginPanel browserPanel;

	private final Map<DefaultMutableTreeNode, OmegaElement> nodeMap;
	private final DefaultMutableTreeNode root;
	private final List<OmegaAnalysisRun> loadedAnalysisRun;

	private final Class<? extends OmegaAnalysisRun> clazz;

	private JTree dataTree;

	public OmegaDataBrowserAnalysisBrowserPanel(final RootPaneContainer parent,
	        final OmegaDataBrowserPluginPanel browserPanel,
	        final Class<? extends OmegaAnalysisRun> clazz,
	        final OmegaAnalysisRunContainer selectedAnalysisContainer,
	        final List<OmegaAnalysisRun> loadedAnalysisRun) {
		super(parent);

		this.loadedAnalysisRun = loadedAnalysisRun;
		this.browserPanel = browserPanel;

		this.clazz = clazz;

		this.root = new DefaultMutableTreeNode();
		this.root.setUserObject("Loaded data");
		this.nodeMap = new HashMap<DefaultMutableTreeNode, OmegaElement>();
		this.updateTree(selectedAnalysisContainer);

		this.setPreferredSize(new Dimension(200, 500));
		this.setSize(new Dimension(200, 500));
		this.setLayout(new BorderLayout());
		this.createAndAddWidgets();
	}

	private void createAndAddWidgets() {
		this.dataTree = new JTree(this.root);
		// this.dataTreeBrowser.setRootVisible(false);
		final CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
		this.dataTree.setCellRenderer(renderer);
		this.dataTree.setCellEditor(new CheckBoxNodeEditor());

		this.dataTree.expandRow(0);
		this.dataTree.setRootVisible(false);
		this.dataTree.setEditable(true);

		final JScrollPane scrollPane = new JScrollPane(this.dataTree);
		scrollPane.setBorder(new TitledBorder(this.clazz.getSimpleName()
		        + " data"));

		this.add(scrollPane, BorderLayout.CENTER);
	}

	private void updateTree(
	        final OmegaAnalysisRunContainer selectedAnalysisContainer) {
		if (selectedAnalysisContainer == null)
			return;
		String s = null;
		CheckBoxStatus status = null;
		this.root.removeAllChildren();
		this.nodeMap.clear();
		for (final OmegaAnalysisRun analysisRun : selectedAnalysisContainer
		        .getAnalysisRuns()) {
			if (!this.clazz.isInstance(analysisRun)) {
				continue;
			}
			final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
			this.nodeMap.put(node, analysisRun);
			s = "[" + analysisRun.getElementID() + "] " + analysisRun.getName();
			status = this.loadedAnalysisRun.contains(analysisRun) ? CheckBoxStatus.SELECTED
			        : CheckBoxStatus.DESELECTED;
			node.setUserObject(new CheckBoxNode(s, status));
		}
	}

	public void fireUpdate(
	        final OmegaAnalysisRunContainer selectedAnalysisContainer) {
		this.dataTree.setRootVisible(true);
		this.updateTree(selectedAnalysisContainer);
		this.dataTree.repaint();
		this.dataTree.expandRow(0);
		this.dataTree.setRootVisible(false);
	}
}