package main;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import forks.Fork;
import forks.ForkController;
import forks.ForkView;
import transaction.TransactionView;
import util.*;
import util.swing.SwingEX;
import util.swing.SwingEX.LTPanel;
import web.XchForks;

@SuppressWarnings("serial")
public class MainGui extends JPanel {
	public static ForkView FV = new ForkView();
	
	// *** Transaction Panel ***
	static JTextField targetAddress = new JTextField(20);
	static JTextField targetAmt = new JTextField(10);
	static JTextField targetFee = new JTextField(5);
	static JButton sendBtn = new JButton(I18n.MainGui.sendBtn);
	private static NetSpace plotSize = new NetSpace("0 TiB");
	private static final JLabel plotlbl = new JLabel(I18n.MainGui.farmSize+" ?");
	private static final JLabel valuelbl = new JLabel(I18n.MainGui.value + Settings.GUI.currencySymbol + "0.0");
	private static final JLabel forklbl = new JLabel(Integer.toString(Fork.LIST.size()) + " " + I18n.MainGui.forks, SwingConstants.CENTER);
	
	GridBagConstraints c = new GridBagConstraints();
	
	public MainGui() {
		setLayout(new BorderLayout());
	
		// *** Transaction Panel ***
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		tPanel.setBorder(new TitledBorder(I18n.MainGui.createTransaction));

		c.fill = GridBagConstraints.BOTH;
		targetFee.setText("0");
		
		c.gridx=1;
        c.weightx=1;
        tPanel.add(targetAddress,c);
        c.gridx=2;
        c.weightx=0;
        tPanel.add(targetAmt,c);
        c.gridx=3;
        c.weightx=0;
        tPanel.add(targetFee,c);
        c.weightx=0;
        c.gridx=4;
        tPanel.add(sendBtn,c);
        c.gridx=5;
        tPanel.add(new SwingEX.Btn("",Ico.GEAR, () -> {
        	JPanel settingPanel = new JPanel(new GridBagLayout());
        	
        	JPanel logReader = new JPanel(new GridLayout(2,1));
        	logReader.setBorder(new TitledBorder(I18n.MainGui.logReader));
        	JPanel daemonReader = new JPanel(new GridLayout(2,1));
        	daemonReader.setBorder(new TitledBorder(I18n.MainGui.daemonReader));
        	JPanel curencyPanel = new JPanel(new GridLayout(1,2));
        	curencyPanel.setBorder(new TitledBorder(I18n.MainGui.currency));
        	JPanel httpServerPanel = new JPanel(new GridLayout(1,2));
        	httpServerPanel.setBorder(new TitledBorder(I18n.MainGui.guiHttpServer));
        	
        	JPanel autoUpdatePanel = new JPanel(new GridLayout(1,2));
        	autoUpdatePanel.setBorder(new TitledBorder(I18n.MainGui.automaticPriceOrColdWalletUpdates));
        	
        	LTPanel lriSleep = new LTPanel(I18n.MainGui.intraDelay , Integer.toString(Settings.GUI.logReaderIntraDelay));
    		LTPanel lreSleep = new LTPanel(I18n.MainGui.exoDelay , Integer.toString(Settings.GUI.logReaderExoDelay));
    		
    		LTPanel dWorkers = new LTPanel(I18n.MainGui.workerThreads , Integer.toString(Settings.GUI.daemonReaderWorkers));
    		LTPanel dIntraSleep = new LTPanel(I18n.MainGui.delay , Integer.toString(Settings.GUI.daemonReaderDelay));
    		
    		LTPanel curencySymbol = new LTPanel(I18n.MainGui.symbol , Settings.GUI.currencySymbol);
    		LTPanel curencyRatio = new LTPanel(I18n.MainGui.ratio , Double.toString(Settings.GUI.currencyRatio));
    		
    		JCheckBox autoUpdate = new JCheckBox(I18n.MainGui.xchforksUrl);
    		autoUpdate.setSelected(Settings.GUI.autoUpdate);
    		
    		JCheckBox lockColumns = new JCheckBox(I18n.MainGui.lockColumns);
    		lockColumns.setSelected(Settings.GUI.lockColumns);
    		
    		JCheckBox httpServerChk = new JCheckBox(I18n.MainGui.enable);
    		LTPanel httpServerPort = new LTPanel(I18n.MainGui.port , Integer.toString(Settings.GUI.httpServerPort));
    		
    		logReader.add(lriSleep);
    		logReader.add(lreSleep);
    		
    		daemonReader.add(dWorkers);
    		daemonReader.add(dIntraSleep);
    		
    		curencyPanel.add(curencySymbol);
    		curencyPanel.add(curencyRatio);
    		
    		autoUpdatePanel.add(autoUpdate);
    		
    		httpServerPanel.add(httpServerChk);
    		httpServerPanel.add(httpServerPort);
    		    		
    		GridBagConstraints gbc = new GridBagConstraints();
    		gbc.fill = GridBagConstraints.HORIZONTAL;
        	settingPanel.add(logReader,gbc);
        	gbc.gridy=1;
        	settingPanel.add(daemonReader,gbc);
        	gbc.gridy=2;
        	settingPanel.add(curencyPanel,gbc);
        	gbc.gridy=3;
        	settingPanel.add(autoUpdatePanel,gbc);
        	gbc.gridy=4;
        	settingPanel.add(httpServerPanel,gbc);
        	gbc.gridy=5;
        	settingPanel.add(lockColumns,gbc);
        	
        	httpServerChk.setSelected(HttpServer.isRunning);
        	httpServerPort.field.setEnabled(!httpServerChk.isSelected());
        	
        	httpServerChk.addActionListener(al -> {
        		if (httpServerChk.isSelected())
        			HttpServer.start(httpServerPort.getAsInt());
        		else
        			HttpServer.stop();
        		httpServerChk.setSelected(HttpServer.isRunning);
        		httpServerPort.field.setEnabled(!httpServerChk.isSelected());
        	});
        	
        	lockColumns.addActionListener(al -> {
        		Settings.GUI.lockColumns = lockColumns.isSelected();
        		setColumnLock(lockColumns.isSelected());
        	});
        	
        	if (true == ForkFarmer.showPopup(I18n.MainGui.settingTitle, settingPanel)) {

        		Settings.GUI.logReaderIntraDelay = lriSleep.getAsInt();
        		Settings.GUI.logReaderExoDelay = lreSleep.getAsInt();
        		Settings.GUI.daemonReaderWorkers = dWorkers.getAsInt();
        		Settings.GUI.daemonReaderDelay = dIntraSleep.getAsInt();
        		Settings.GUI.autoUpdate = autoUpdate.isSelected();
        		Settings.GUI.httpServerPort =  httpServerPort.getAsInt();
        		
        		if (!Settings.GUI.currencySymbol.equals(curencySymbol.getText()))
        			Settings.GUI.currencySymbol = curencySymbol.getText();
        		Settings.GUI.currencyRatio = curencyRatio.getAsDouble();
        		XchForks.updatePricesForced();
        	}
        		
        }));
        
        if (Settings.GUI.lockColumns)
        	setColumnLock(Settings.GUI.lockColumns);
        
        sendBtn.addActionListener(e -> sendTx());
        		
        JPanel PEPanel = new JPanel(new BorderLayout());
        PEPanel.add(tPanel,BorderLayout.PAGE_START);
        PEPanel.add(new TransactionView(),BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel topCenter = new JPanel(new BorderLayout());
        topCenter.add(forklbl, BorderLayout.CENTER);
        
        topPanel.add(valuelbl,BorderLayout.LINE_START);
        topPanel.add(topCenter, BorderLayout.CENTER);
        topPanel.add(plotlbl,BorderLayout.LINE_END);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        	splitPane.setTopComponent(FV);
        	splitPane.setBottomComponent(PEPanel);
        add (topPanel,BorderLayout.PAGE_START);
        add (splitPane, BorderLayout.CENTER);
        
        updatePlotSize(plotSize);
        			
		// compute fork refresh delay
		new Thread(ForkController::daemonReader).start();
		new Thread(ForkController::logReader).start();
		ForkView.TABLE.setAutoCreateRowSorter(true);
		
		Args.handle();
	}
	
	private void setColumnLock(boolean locked) {
		ForkView.TABLE.getTableHeader().setReorderingAllowed(!locked);
		ForkView.TABLE.getTableHeader().setResizingAllowed(!locked);
		TransactionView.TABLE.getTableHeader().setReorderingAllowed(!locked);
		TransactionView.TABLE.getTableHeader().setResizingAllowed(!locked);
	}
	
	private void sendTx() {
		String address = targetAddress.getText();
		
		Fork.getByAddress(address).ifPresentOrElse(f -> {
			f.sendTX(address,targetAmt.getText(),targetFee.getText());
			targetAddress.setText("");
			targetAmt.setText("");
		}, 	() -> ForkFarmer.showMsg(I18n.MainGui.errorTitle, I18n.MainGui.errorContent));
		
	}
	public static void updatePlotSize(NetSpace ps) {
		if (ps.szTB > plotSize.szTB) {
			plotSize = ps;
			plotlbl.setText(I18n.MainGui.farmSize + ps.toString());
		}
	}

	public static void updateTotal() {
		double totalValue = 0;

		for (Fork f : Fork.LIST)
			totalValue += f.equity.amt;
		
		valuelbl.setText(I18n.MainGui.value + Settings.GUI.currencySymbol + Util.round(totalValue, 2));
	}
	
	public static void updateNumForks() {
		forklbl.setText(Integer.toString(Fork.LIST.size()) + " " + I18n.MainGui.forks);
	}
	
}
