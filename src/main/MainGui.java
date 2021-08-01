package main;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import forks.Fork;
import forks.ForkView;
import transaction.TransactionView;
import util.NetSpace;
import util.Util;
import util.process.ProcessPiper;

@SuppressWarnings("serial")
public class MainGui extends JPanel {
	public static int numForks;
	public static ForkView FV = new ForkView();
	
	// *** Transaction Panel ***
	static JTextField targetAddress = new JTextField(20);
	static JTextField targetAmt = new JTextField(10);
	static JTextField targetFee = new JTextField(5);
	static JButton sendBtn = new JButton("Send");
	private static NetSpace plotSize = new NetSpace("0 TiB");
	private static final JLabel plotlbl = new JLabel("Farm Size: ?");
	
	GridBagConstraints c = new GridBagConstraints();
	
	public MainGui() {
		setLayout(new BorderLayout());
	
		// *** Transaction Panel ***
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		tPanel.setBorder(new TitledBorder("Create Transaction: (address, amt, fee)"));

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
        
        sendBtn.addActionListener(e -> sendTx());
        		
        JPanel PEPanel = new JPanel(new BorderLayout());
        PEPanel.add(tPanel,BorderLayout.PAGE_START);
        PEPanel.add(new TransactionView(),BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(plotlbl,BorderLayout.LINE_END);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        	splitPane.setTopComponent(FV);
        	splitPane.setBottomComponent(PEPanel);
        add (topPanel,BorderLayout.PAGE_START);
        add (splitPane, BorderLayout.CENTER);
        
        updatePlotSize(plotSize);
        			
		Fork.LIST.clear();
		Fork.factory("XCH","Chia");
		Fork.factory("XFX","Flax");
		
		Fork.factory("XFL","Flora");
		Fork.factory("CGN","Chaingreen","Chaingreen");
		Fork.factory("XGJ","Goji","goji-blockchain");
		Fork.factory("XSE","Seno","seno2");
		Fork.factory("AVO","Avocado");
		Fork.factory("XKA","Kale");
		Fork.factory("XTX","Taco");
		Fork.factory("XDG","DogeChia");
		
		
		Fork.factory("XCR","Chiarose","Chiarose","chia-rosechain");
		Fork.factory("SIT","Silicoin");
		Fork.factory("XCD","Chiadoge","Chiadoge","Chiadoge");
		Fork.factory("GDOG","Greendoge");
		Fork.factory("HDD","Hddcoin");
		
		Fork.factory("XEQ","Equality");
		Fork.factory("SOCK","Socks");
		Fork.factory("WHEAT","Wheat");
		Fork.factory("XMX","Melati");
		
		Fork.factory("SPARE","Spare");
		Fork.factory("TAD","Tad");
		
		Fork.factory("CANS", "Cannabis");
		Fork.factory("XSC","Sector");
		Fork.factory("CAC","Cactus");
		Fork.factory("CHIVES","Chives");
		Fork.factory("VAG","Cunt");
		Fork.factory("XCL","Caldera");
		
		Fork.factory("APPLE","Apple");
		Fork.factory("XMZ","Maize");
		Fork.factory("COV","Covid");
		Fork.factory("SRN","Shamrock");
		Fork.factory("XFK","Fork");
		
		// compute fork refresh delay
		
		long delayStep = (long)(120/Fork.LIST.size());
		long delay = 0;
		for (Fork f: Fork.LIST) {
			Fork.SVC.submit(() -> f.loadWallet());
			f.future =  Fork.SVC.scheduleAtFixedRate(() -> f.loadWallet(), delay, 120, TimeUnit.SECONDS);
			delay += delayStep;
		}
		
		numForks = Fork.LIST.size();
		ForkView.TABLE.setAutoCreateRowSorter(true);
		FV.setBorder(new TitledBorder(numForks + " Forks Intalled" ));
		
		if (ForkFarmer.args.length > 0)
			Executors.newSingleThreadScheduledExecutor().submit(MainGui::argStart);
	}
	
	
	private static void argStart() {
		for (String s: ForkFarmer.args) {
			for (Fork f: Fork.LIST) {
				if (f.symbol.equals(s)) {
					System.out.println("Launching: " + f.exePath + " start farmer");
					ProcessPiper.run(f.exePath,"start","farmer");
					Util.sleep(30000);
				}
			}
		}
		System.out.println("done launching forks... exiting");
		System.exit(0);
	}
	
	private void sendTx() {
		String address = targetAddress.getText();
		
		for (Fork f : Fork.LIST) {
			if (address.startsWith(f.symbol.toLowerCase())) {
				f.sendTX(address,targetAmt.getText(),targetFee.getText());
				return;
			}
		}
		
		ForkFarmer.showMsg("Error", "No suitable fork found for address prefix");
	}
	public static void updatePlotSize(NetSpace ps) {
		if (ps.szTB > plotSize.szTB) {
			plotSize = ps;
			plotlbl.setText("Farm Size: " + ps.toString());
		}
	}
	
	/*
	private static void logReader() {
		for (Fork f : Fork.LIST)
			Fork.SVC.submit(() -> f.readLog());
	}
	*/
}
