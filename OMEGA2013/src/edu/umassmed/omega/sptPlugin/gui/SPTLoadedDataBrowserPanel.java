package edu.umassmed.omega.sptPlugin.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.RootPaneContainer;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.lf5.viewer.categoryexplorer.TreeModelAdapter;

import edu.umassmed.omega.commons.gui.GenericPanel;
import edu.umassmed.omega.commons.gui.checkboxTree.CheckBoxNode;
import edu.umassmed.omega.commons.gui.checkboxTree.CheckBoxStatus;
import edu.umassmed.omega.dataNew.analysisRunElements.OmegaAnalysisRun;
import edu.umassmed.omega.dataNew.coreElements.OmegaElement;
import edu.umassmed.omega.dataNew.coreElements.OmegaImage;

public class SPTLoadedDataBrowserPanel extends GenericPanel {

	private static final long serialVersionUID = -7554854467725521545L;

	private final SPTPluginPanel sptPanel;

	private final Map<DefaultMutableTreeNode, OmegaElement> nodeMap;
	private final DefaultMutableTreeNode root;

	private JTree dataTree;

	private boolean adjusting = false;

	public SPTLoadedDataBrowserPanel(final RootPaneContainer parentContainer,
	        final SPTPluginPanel sptPanel) {
		super(parentContainer);

		this.sptPanel = sptPanel;

		this.root = new DefaultMutableTreeNode();
		this.root.setUserObject("Loaded data");
		this.nodeMap = new HashMap<DefaultMutableTreeNode, OmegaElement>();
		// this.updateTree(images);

		this.setLayout(new BorderLayout());

		this.createAndAddWidgets();
		this.addListeners();
	}

	private void createAndAddWidgets() {

		this.dataTree = new JTree(this.root);
		// this.dataTreeBrowser.setRootVisible(false);
		// final CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
		// this.dataTree.setCellRenderer(renderer);
		// this.dataTree.setCellEditor(new CheckBoxNodeEditor());

		this.dataTree.setEditable(false);

		this.dataTree.expandRow(0);
		this.dataTree.setRootVisible(false);
		// this.dataTree.setEditable(true);

		final JScrollPane scrollPane = new JScrollPane(this.dataTree);
		scrollPane.setBorder(new TitledBorder("Loaded data"));

		this.add(scrollPane, BorderLayout.CENTER);
	}

	private void addListeners() {
		this.dataTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent event) {
				final TreePath path = SPTLoadedDataBrowserPanel.this.dataTree
				        .getPathForLocation(event.getX(), event.getY());
				if (path == null) {
					SPTLoadedDataBrowserPanel.this.sptPanel
					        .updateSelectedImage(null);
					SPTLoadedDataBrowserPanel.this.deselect();
					return;
				}
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
				        .getLastPathComponent();
				final OmegaElement element = SPTLoadedDataBrowserPanel.this.nodeMap
				        .get(node);
				if (element instanceof OmegaImage) {
					SPTLoadedDataBrowserPanel.this.sptPanel
					        .updateSelectedImage((OmegaImage) element);
				} else if (element instanceof OmegaAnalysisRun) {
					final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node
					        .getParent();
					final OmegaElement parentElement = SPTLoadedDataBrowserPanel.this.nodeMap
					        .get(parentNode);
					SPTLoadedDataBrowserPanel.this.sptPanel
					        .updateSelectedImage((OmegaImage) parentElement);
					SPTLoadedDataBrowserPanel.this.sptPanel
					        .updateSelectedAnalysisRun((OmegaAnalysisRun) element);
				}
			}
		});
		this.dataTree.getModel().addTreeModelListener(new TreeModelAdapter() {
			@Override
			public void treeNodesChanged(final TreeModelEvent event) {
				if (SPTLoadedDataBrowserPanel.this.adjusting)
					return;
				SPTLoadedDataBrowserPanel.this.adjusting = true;
				final TreePath parent = event.getTreePath();
				final Object[] children = event.getChildren();
				final DefaultTreeModel model = (DefaultTreeModel) event
				        .getSource();

				DefaultMutableTreeNode node;
				CheckBoxNode c; // = (CheckBoxNode)node.getUserObject();
				if ((children != null) && (children.length == 1)) {
					node = (DefaultMutableTreeNode) children[0];
					c = (CheckBoxNode) node.getUserObject();
					final DefaultMutableTreeNode n = (DefaultMutableTreeNode) parent
					        .getLastPathComponent();

					model.nodeChanged(n);
				} else {
					node = (DefaultMutableTreeNode) model.getRoot();
					c = (CheckBoxNode) node.getUserObject();
				}

				model.nodeChanged(node);

				SPTLoadedDataBrowserPanel.this.adjusting = false;

				c.getStatus();
				// TODO update something here
			}
		});
	}

	@Override
	public void updateParentContainer(final RootPaneContainer parent) {
		super.updateParentContainer(parent);
	}

	private void updateTree(final List<OmegaImage> images) {
		String s = null;
		final CheckBoxStatus status = CheckBoxStatus.DESELECTED;
		this.root.removeAllChildren();
		((DefaultTreeModel) this.dataTree.getModel()).reload();
		this.nodeMap.clear();
		if (images == null)
			return;
		for (final OmegaImage image : images) {
			final DefaultMutableTreeNode imageNode = new DefaultMutableTreeNode();
			this.nodeMap.put(imageNode, image);
			s = "[" + image.getElementID() + "] " + image.getName();
			// status = this.loadedData.containsImage(image) ?
			// CheckBoxStatus.SELECTED
			// : CheckBoxStatus.DESELECTED;
			imageNode.setUserObject(new CheckBoxNode(s, status));
			this.root.add(imageNode);
		}
	}

	public void update(final List<OmegaImage> images) {
		this.dataTree.setRootVisible(true);
		this.updateTree(images);
		this.dataTree.repaint();
		this.dataTree.expandRow(0);
		this.dataTree.setRootVisible(false);
	}

	public void deselect() {
		this.dataTree.setSelectionRow(-1);
	}
}