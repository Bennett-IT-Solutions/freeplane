/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.controller;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.freeplane.controller.filter.FilterController;
import org.freeplane.controller.help.HelpController;
import org.freeplane.controller.print.PrintController;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.controller.views.MapViewManager;
import org.freeplane.controller.views.ViewController;
import org.freeplane.extension.ExtensionHashMap;
import org.freeplane.extension.IExtension;
import org.freeplane.map.attribute.ModelessAttributeController;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.modes.ModeController;

/**
 * Provides the methods to edit/change a Node. Forwards all messages to
 * MapModel(editing) or MapView(navigation).
 */
public class Controller {
	private static Controller controllerInstance;
	public static final String JAVA_VERSION = System.getProperty("java.version");
	public static final String ON_START_IF_NOT_SPECIFIED = "on_start_if_not_specified";
	private static ResourceController resourceController;
	public static final FreemindVersionInformation VERSION = new FreemindVersionInformation(
	    "0.9.0 Freeplane 21");
	public static final String XML_VERSION = "0.9.0";

	public static Controller getController() {
		return controllerInstance;
	}

	/** @return the current modeController. */
	public static ModeController getModeController() {
		return controllerInstance.modeController;
	}

	static public ResourceController getResourceController() {
		return resourceController;
	}

	public static String getText(final String string) {
		return string == null ? null : resourceController.getText(string);
	}

	public static boolean isMacOsX() {
		boolean underMac = false;
		final String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			underMac = true;
		}
		return underMac;
	}

	private final ActionController actionController;
	private ModelessAttributeController attributeController;
	final private ExtensionHashMap extensions;
	private FilterController filterController;
	private HelpController helpController;
	/**
	 * Converts from a local link to the real file URL of the documentation map.
	 * (Used to change this behaviour under MacOSX).
	 */
	private ModeController modeController;
	final private HashMap<String, ModeController> modeControllers;
	private PrintController printController;
	final private Action quit;
	private ViewController viewController;

	public Controller(final ResourceController resourceController) {
		if (Controller.controllerInstance != null) {
			throw new RuntimeException("Controller already created");
		}
		Controller.resourceController = resourceController;
		Controller.controllerInstance = this;
		extensions = new ExtensionHashMap();
		actionController = new ActionController();
		modeControllers = new HashMap();
		quit = new QuitAction(resourceController);
		addAction("quit", quit);
		resourceController.init();
	}

	public void addAction(final Object key, final Action value) {
		actionController.addAction(key, value);
	}

	public boolean addExtension(final Class clazz, final IExtension extension) {
		return extensions.addExtension(clazz, extension);
	}

	public boolean addExtension(final IExtension extension) {
		return extensions.addExtension(extension);
	}

	public void addModeController(final ModeController modeController) {
		modeControllers.put(modeController.getModeName(), modeController);
	}

	/**
	 * Closes the actual map.
	 *
	 * @param force
	 *            true= without save.
	 */
	public void close(final boolean force) {
		getMapViewManager().close(force);
	}

	public void errorMessage(final Object message) {
		String myMessage = "";
		if (message != null) {
			myMessage = message.toString();
		}
		else {
			myMessage = Controller.getText("undefined_error");
			if (myMessage == null) {
				myMessage = "Undefined error";
			}
		}
		JOptionPane.showMessageDialog(Controller.getController().getViewController()
		    .getContentPane(), myMessage, "FreeMind", JOptionPane.ERROR_MESSAGE);
	}

	public void errorMessage(final Object message, final JComponent component) {
		JOptionPane.showMessageDialog(component, message.toString(), "FreeMind",
		    JOptionPane.ERROR_MESSAGE);
	}

	public Action getAction(final String key) {
		return actionController.getAction(key);
	}

	public ModelessAttributeController getAttributeController() {
		return attributeController;
	}

	public FilterController getFilterController() {
		return filterController;
	}

	public FreemindVersionInformation getFreemindVersion() {
		return Controller.VERSION;
	}

	public HelpController getHelpController() {
		return helpController;
	}

	/**
	 * @return
	 */
	public MapModel getMap() {
		return getViewController().getMap();
	}

	/**
	 * @return
	 */
	public MapView getMapView() {
		return getViewController().getMapView();
	}

	public MapViewManager getMapViewManager() {
		return getViewController().getMapViewManager();
	}

	public ModeController getModeController(final String modeName) {
		return modeControllers.get(modeName);
	}

	/** Returns the current model */
	public MapModel getModel() {
		if (getMapView() != null) {
			return getMapView().getModel();
		}
		return null;
	}

	public Set getModes() {
		return modeControllers.keySet();
	}

	public PrintController getPrintController() {
		return printController;
	}

	/**
	 * @return
	 */
	public ViewController getViewController() {
		return viewController;
	}

	public void informationMessage(final Object message) {
		JOptionPane.showMessageDialog(Controller.getController().getViewController()
		    .getContentPane(), message.toString(), "FreeMind", JOptionPane.INFORMATION_MESSAGE);
	}

	public void informationMessage(final Object message, final JComponent component) {
		JOptionPane.showMessageDialog(component, message.toString(), "FreeMind",
		    JOptionPane.INFORMATION_MESSAGE);
	}

	void quit() {
		final String currentMapRestorable = Controller.getModeController().getUrlManager()
		    .getRestoreable(getMap());
		if (!getViewController().quit()) {
			return;
		}
		if (currentMapRestorable != null) {
			Controller.getResourceController().setProperty(Controller.ON_START_IF_NOT_SPECIFIED,
			    currentMapRestorable);
		}
		if (modeController != null) {
			modeController.shutdown();
		}
		Controller.getController().getViewController().exit();
	}

	/**
	 * @param actionEvent
	 */
	public void quit(final ActionEvent actionEvent) {
		quit.actionPerformed(actionEvent);
	}

	public Action removeAction(final String key) {
		return actionController.removeAction(key);
	}

	public IExtension removeExtension(final Class clazz) {
		return extensions.removeExtension(clazz);
	}

	public boolean removeExtension(final IExtension extension) {
		return extensions.removeExtension(extension);
	}

	public void selectMode(final ModeController newModeController) {
		if (modeController == newModeController) {
			return;
		}
		if (modeController != null) {
			modeController.shutdown();
		}
		viewController.selectMode(modeController, newModeController);
		modeController = newModeController;
		newModeController.startup();
	}

	public boolean selectMode(final String modeName) {
		final ModeController newModeController = modeControllers.get(modeName);
		if (modeController == newModeController) {
			return true;
		}
		selectMode(newModeController);
		return getMapViewManager().changeToMode(modeName);
	}

	public void setAttributeController(final ModelessAttributeController attributeController) {
		this.attributeController = attributeController;
	}

	public void setFilterController(final FilterController filterController) {
		this.filterController = filterController;
	}

	public void setHelpController(final HelpController helpController) {
		this.helpController = helpController;
	}

	public void setPrintController(final PrintController printController) {
		this.printController = printController;
	}

	public void setViewController(final ViewController viewController) {
		this.viewController = viewController;
	}
}
