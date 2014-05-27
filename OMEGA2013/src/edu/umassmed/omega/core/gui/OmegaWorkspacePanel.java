package edu.umassmed.omega.core.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.RootPaneContainer;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import edu.umassmed.omega.commons.OmegaPlugin;
import edu.umassmed.omega.commons.exceptions.OmegaMissingData;
import edu.umassmed.omega.commons.gui.GenericDesktopPane;
import edu.umassmed.omega.commons.gui.GenericPanelInterface;
import edu.umassmed.omega.commons.gui.GenericPluginPanel;

public class OmegaWorkspacePanel extends GenericDesktopPane implements
        GenericPanelInterface {
	private static final long serialVersionUID = -2466542815630183505L;

	private RootPaneContainer parent;

	private int panelIndex;

	private final Map<OmegaPlugin, Integer> startingIndex;
	private final Map<Integer, GenericPluginPanel> contents;
	private final Map<Integer, Boolean> visibilities;
	private final Map<Integer, JMenuBar> menus;
	private final Map<Integer, JInternalFrame> internalFrames;
	private final Map<Integer, JFrame> frames;

	private boolean isAttached;

	private boolean hasAttachedFrame;

	private JMenuBar menu;
	private JMenu workspaceMenu;
	private JMenu fileMenu, windowsMenu;
	private JMenuItem quitMItem, attachAllWindows, detachAllWindows;

	// private JDesktopPane desktopPane;

	public OmegaWorkspacePanel(final JFrame parent) {
		this.parent = parent;

		this.panelIndex = 0;

		this.startingIndex = new HashMap<OmegaPlugin, Integer>();
		this.contents = new HashMap<Integer, GenericPluginPanel>();
		this.visibilities = new HashMap<Integer, Boolean>();
		this.menus = new HashMap<Integer, JMenuBar>();
		this.internalFrames = new HashMap<Integer, JInternalFrame>();
		this.frames = new HashMap<Integer, JFrame>();

		this.isAttached = true;
		this.hasAttachedFrame = true;

		this.setPreferredSize(new Dimension(1000, 1000));

		this.createAndAddWidgets();
		this.createMenu();
	}

	private void createAndAddWidgets() {
		// this.desktopPane = new JDesktopPane();
		// this.getViewport().add(this.desktopPane);
	}

	private void createMenu() {
		this.menu = new JMenuBar();
		this.workspaceMenu = new JMenu("Workspace");

		// this.fileMenu = new JMenu("File"); this.quitMItem = new
		// JMenuItem("Quit"); this.fileMenu.add(this.quitMItem);

		this.windowsMenu = new JMenu("Window");
		this.attachAllWindows = new JMenuItem("Attached all windows");
		this.windowsMenu.add(this.attachAllWindows);
		this.detachAllWindows = new JMenuItem("Detached all windows");
		this.windowsMenu.add(this.detachAllWindows);

		// this.menu.add(this.fileMenu);
		this.menu.add(this.windowsMenu);
		this.workspaceMenu.add(this.windowsMenu);
	}

	public JMenuBar getMenuBar() {
		return this.menu;
	}

	public JMenu getMenu() {
		return this.workspaceMenu;
	}

	@Override
	public void updateParentContainer(final RootPaneContainer parent) {
		this.parent = parent;
	}

	protected void initializePanel() {
		this.createAndAddWidgets();

		this.addListeners();
	}

	private void addListeners() {
		this.addContainerListener(new ContainerListener() {
			@Override
			public void componentRemoved(final ContainerEvent evt) {
				// if (evt.getChild() instanceof JInternalFrame) {
				// final JInternalFrame intFrame = (JInternalFrame) evt
				// .getChild();
				// for (final GenericPluginPanel content :
				// OmegaWorkspacePanel.this.contents) {
				// if (intFrame.equals(OmegaWorkspacePanel.this.internalFrames
				// .get(content))) {
				// OmegaWorkspacePanel.this.visibilities.put(
				// content.getIndex(), false);
				// }
				// }
				// }
			}

			@Override
			public void componentAdded(final ContainerEvent evt) {

			}
		});

		this.detachAllWindows.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				OmegaWorkspacePanel.this.hasAttachedFrame = false;
				for (final Integer index : OmegaWorkspacePanel.this.contents
				        .keySet()) {
					final GenericPluginPanel content = OmegaWorkspacePanel.this.contents
					        .get(index);
					if (content.isAttached()) {
						OmegaWorkspacePanel.this.detachFrame(content);
					}
				}
			}
		});

		this.attachAllWindows.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				OmegaWorkspacePanel.this.hasAttachedFrame = true;
				for (final Integer index : OmegaWorkspacePanel.this.contents
				        .keySet()) {
					final GenericPluginPanel content = OmegaWorkspacePanel.this.contents
					        .get(index);
					if (!content.isAttached()) {
						OmegaWorkspacePanel.this.attachFrame(content);
					}
				}
			}
		});

		this.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(
				        OmegaGUIFrame.PROP_TOGGLEWINDOW)) {
					final GenericPluginPanel content = OmegaWorkspacePanel.this.contents
					        .get(Integer.valueOf(evt.getNewValue().toString()));
					evt.getNewValue();
					if (content.isAttached()) {
						OmegaWorkspacePanel.this.detachFrame(content);
					} else {
						OmegaWorkspacePanel.this.attachFrame(content);
					}
				}
			}
		});
	}

	public boolean isAttached() {
		return this.isAttached;
	}

	public void setAttached(final boolean tof) {
		this.isAttached = tof;
	}

	public void showPlugin(final OmegaPlugin plugin) {

		// TODO Controllo se ho max 1 panello,
		// Nel caso lo mostro
		// Altrimenti apro dialog per sapere se ne vuole aprire
		// Una nuova
		// Se ho raggiunto il max invece mostro e basta
		if (plugin.maximumReached()) {
			this.showAllPanels(plugin);
		} else {
			this.showNewPanel(plugin);
		}
	}

	public void showAllPanels(final OmegaPlugin plugin) {
		for (final Integer index : plugin.getIndexes()) {
			if (this.internalFrames.containsKey(index)) {
				final JInternalFrame intFrame = this.internalFrames.get(index);
				intFrame.setVisible(true);
				intFrame.requestFocus();

			} else if (this.frames.containsKey(index)) {
				final JFrame frame = this.frames.get(index);
				frame.setVisible(true);
				frame.setVisible(true);
				frame.requestFocus();
			}
		}
	}

	public void showNewPanel(final OmegaPlugin plugin) {
		int startingIndex = -1;

		if (this.startingIndex.containsKey(plugin)) {
			startingIndex = this.startingIndex.get(plugin);
		} else {
			startingIndex = this.panelIndex;
			this.panelIndex += 100;
			this.startingIndex.put(plugin, startingIndex);
		}

		if (this.hasAttachedFrame) {
			this.createNewInternalFrame(plugin, startingIndex);
		} else {
			this.createNewFrame(plugin, startingIndex);
		}
	}

	private void createNewInternalFrame(final OmegaPlugin plugin,
	        final int startingIndex) {
		final String name = "Workspace - " + plugin.getName();
		final JInternalFrame intFrame = new JInternalFrame(name, true, true,
		        true, true);

		GenericPluginPanel content = null;
		try {
			content = plugin.getNewPanel(intFrame, startingIndex);
		} catch (final OmegaMissingData e) {
			e.printStackTrace();
			// TODO inserire warning
			return;
		}
		if (content == null) {
			// TODO inserire warning
			System.out.println(name + " MAXIMUM REACHED");
			return;
		}
		content.setIsAttached(true);
		final int index = content.getIndex();
		content.setName(name);
		final JMenuBar menuBar = content.getMenu();

		intFrame.setLayout(new BorderLayout());
		intFrame.add(content, BorderLayout.CENTER);
		intFrame.setPreferredSize(content.getPreferredSize());
		intFrame.setJMenuBar(menuBar);
		intFrame.setLocation(50, 50);
		intFrame.setVisible(true);
		intFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		intFrame.pack();

		this.addInternalFrameListeners(intFrame);

		// this.desktopPane.add(aFrame);
		this.add(intFrame);
		this.contents.put(index, content);
		this.visibilities.put(index, true);
		this.internalFrames.put(index, intFrame);
		this.menus.put(index, menuBar);

		this.validate();
		this.repaint();
	}

	private void createInternalFrame(final GenericPluginPanel content) {
		final int index = content.getIndex();
		final JInternalFrame intFrame = new JInternalFrame(content.getName(),
		        true, true, true, true);
		content.updateParentContainer(intFrame);
		final JMenuBar menuBar = this.menus.get(index);

		intFrame.setLayout(new BorderLayout());
		intFrame.add(content, BorderLayout.CENTER);
		intFrame.setPreferredSize(content.getPreferredSize());
		intFrame.setJMenuBar(menuBar);
		intFrame.setLocation(50, 50);
		intFrame.setVisible(true);
		intFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		intFrame.pack();

		this.addInternalFrameListeners(intFrame);

		// this.desktopPane.add(aFrame);
		this.add(intFrame);
		this.visibilities.put(index, true);
		this.internalFrames.put(index, intFrame);
		// this.menus.put(content.getIndex(), content.getMenu());

		this.validate();
		this.repaint();
	}

	private void createNewFrame(final OmegaPlugin plugin,
	        final int startingIndex) {
		final String name = "Workspace - " + plugin.getName();
		final JFrame frame = new JFrame(name);

		GenericPluginPanel content = null;
		try {
			content = plugin.getNewPanel(frame, startingIndex);
		} catch (final OmegaMissingData e) {
			// TODO inserire warning
			e.printStackTrace();
			return;
		}
		if (content == null) {
			// TODO inserire warning
			System.out.println(name + " MAXIMUM REACHED");
			return;
		}
		content.setIsAttached(false);
		final int index = content.getIndex();
		content.setName(name);
		final JMenuBar menuBar = content.getMenu();

		frame.setLayout(new BorderLayout());
		frame.add(content, BorderLayout.CENTER);
		frame.setPreferredSize(content.getPreferredSize());
		frame.setJMenuBar(menuBar);
		frame.setLocation(50, 50);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		frame.pack();

		this.addFrameListeners(frame);

		// this.desktopPane.add(aFrame);
		// this.add(frame);
		this.contents.put(index, content);
		this.visibilities.put(index, true);
		this.frames.put(index, frame);
		this.menus.put(index, menuBar);
	}

	private void createFrame(final GenericPluginPanel content) {
		final int index = content.getIndex();
		final JFrame frame = new JFrame(content.getName());
		content.updateParentContainer(frame);
		final JMenuBar menuBar = this.menus.get(index);

		frame.setLayout(new BorderLayout());
		frame.add(content, BorderLayout.CENTER);
		frame.setPreferredSize(content.getPreferredSize());
		frame.setJMenuBar(menuBar);
		frame.setLocation(50, 50);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		frame.pack();

		this.addFrameListeners(frame);

		// this.desktopPane.add(aFrame);
		// this.add(frame);
		this.visibilities.put(index, true);
		this.frames.put(index, frame);
		// this.menus.put(content.getIndex(), content.getMenu());
	}

	private void checkWindowMenu(final JMenuBar menuBar) {
		JMenu windowMenu = null;
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			final JMenu menu = menuBar.getMenu(i);
			if (menu.getText().equals("Window")) {
				windowMenu = menu;
			}
		}
		if (windowMenu == null) {
			windowMenu = new JMenu();
		}
		final JMenuItem toggleWindowPositionMenuItem = new JMenuItem(
		        "Toggle window position");
		toggleWindowPositionMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {

			}
		});
	}

	private void addInternalFrameListeners(final JInternalFrame intFrame) {
		final GenericPluginPanel pluginPanel = (GenericPluginPanel) intFrame
		        .getContentPane().getComponent(0);
		intFrame.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(final InternalFrameEvent evt) {
				pluginPanel.onCloseOperation();
			}
		});

		intFrame.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(
				        OmegaGUIFrame.PROP_TOGGLEWINDOW)) {
					OmegaWorkspacePanel.this.firePropertyChange(
					        OmegaGUIFrame.PROP_TOGGLEWINDOW, evt.getOldValue(),
					        evt.getNewValue());
				}
			}
		});
	}

	private void addFrameListeners(final JFrame frame) {
		final GenericPluginPanel pluginPanel = (GenericPluginPanel) frame
		        .getContentPane().getComponent(0);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent evt) {
				pluginPanel.onCloseOperation();
			}
		});
		frame.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(
				        OmegaGUIFrame.PROP_TOGGLEWINDOW)) {
					OmegaWorkspacePanel.this.firePropertyChange(
					        OmegaGUIFrame.PROP_TOGGLEWINDOW, evt.getOldValue(),
					        evt.getNewValue());
				}
			}
		});
	}

	protected void detachFrame(final GenericPluginPanel content) {
		final int index = content.getIndex();
		final JInternalFrame intFrame = OmegaWorkspacePanel.this.internalFrames
		        .get(index);
		this.remove(intFrame);
		intFrame.remove(content);

		this.createFrame(content);

		this.internalFrames.remove(index);
		intFrame.dispose();

		content.setIsAttached(false);
		this.validate();
		this.repaint();
	}

	protected void attachFrame(final GenericPluginPanel content) {
		final int index = content.getIndex();
		final JFrame frame = OmegaWorkspacePanel.this.frames.get(index);
		frame.setVisible(false);
		frame.getContentPane().remove(content);

		this.createInternalFrame(content);

		this.frames.remove(index);
		frame.dispose();
		content.setIsAttached(true);
	}
}