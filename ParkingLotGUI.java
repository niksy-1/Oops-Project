import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

public class ParkingLotGUI extends JFrame {
    private final GateSystem gateSystem;

    private final JTextField plateInput = new JTextField(14);
    private final JComboBox<String> typeSelect = new JComboBox<>(new String[]{"Two Wheeler", "Four Wheeler"});
    private final JTextField ticketIdInput = new JTextField(10);
    private final JTextField searchPlateInput = new JTextField(14);

    private final JTextArea statusArea = new JTextArea();
    private final DefaultListModel<String> activeTicketsModel = new DefaultListModel<>();

    public ParkingLotGUI(int lotSize, boolean testMode) {
        this.gateSystem = new GateSystem(lotSize);
        ParkingUtils.setTimeScale(testMode ? 300 : 1);

        setTitle("Parking Lot Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);

        buildUI();
        refreshViews();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

        actions.add(createParkPanel());
        actions.add(createUnparkPanel());
        actions.add(createFindPanel());
        actions.add(createUtilityPanel());

        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);

        JList<String> ticketList = new JList<>(activeTicketsModel);

        JPanel rightPane = new JPanel(new GridLayout(2, 1, 10, 10));
        rightPane.add(wrapWithTitle("Parking Slot Status", new JScrollPane(statusArea)));
        rightPane.add(wrapWithTitle("Active Tickets", new JScrollPane(ticketList)));

        root.add(actions, BorderLayout.WEST);
        root.add(rightPane, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createParkPanel() {
        JButton parkButton = new JButton("Park Vehicle");
        parkButton.addActionListener(e -> handlePark());

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("License Plate"));
        panel.add(plateInput);
        panel.add(new JLabel("Vehicle Type"));
        panel.add(typeSelect);
        panel.add(parkButton);
        return wrapWithTitle("Park", panel);
    }

    private JPanel createUnparkPanel() {
        JButton unparkButton = new JButton("Unpark by Ticket ID");
        unparkButton.addActionListener(e -> handleUnpark());

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Ticket ID"));
        panel.add(ticketIdInput);
        panel.add(unparkButton);
        return wrapWithTitle("Unpark", panel);
    }

    private JPanel createFindPanel() {
        JButton findButton = new JButton("Find Active Ticket");
        findButton.addActionListener(e -> handleFindTicket());

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("License Plate"));
        panel.add(searchPlateInput);
        panel.add(findButton);
        return wrapWithTitle("Ticket Lookup", panel);
    }

    private JPanel createUtilityPanel() {
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshViews());

        JButton dumpButton = new JButton("Dump Active Tickets");
        dumpButton.addActionListener(e -> {
            boolean dumped = gateSystem.dumpActiveTicketsToLog();
            showInfo(dumped ? "Active tickets dumped to logs." : "No active tickets to dump.");
            refreshViews();
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(refreshButton);
        panel.add(dumpButton);
        panel.setPreferredSize(new Dimension(260, 90));
        return wrapWithTitle("Utilities", panel);
    }

    private void handlePark() {
        String plate = ParkingUtils.normalizePlate(plateInput.getText().trim());
        if (!ParkingUtils.isValidPlateFormat(plate)) {
            showError("Invalid plate format. Expected: AA11AA1111");
            return;
        }

        VehicleType type = typeSelect.getSelectedIndex() == 0 ? VehicleType.TWO_WHEELER : VehicleType.FOUR_WHEELER;
        String ticketId = gateSystem.handleParking(plate, type);

        if (ticketId.isEmpty()) {
            showError("Parking full or duplicate active vehicle.");
            return;
        }

        showInfo("Vehicle parked. Ticket ID: " + ticketId);
        plateInput.setText("");
        refreshViews();
    }

    private void handleUnpark() {
        String ticketId = ticketIdInput.getText().trim();
        if (ticketId.isEmpty()) {
            showError("Please enter a ticket ID.");
            return;
        }

        String receipt = gateSystem.handleUnparkingWithReceipt(ticketId);
        if (receipt == null) {
            showError("Invalid ticket ID.");
            return;
        }

        JTextArea receiptArea = new JTextArea(receipt);
        receiptArea.setEditable(false);
        receiptArea.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(receiptArea), "Receipt", JOptionPane.INFORMATION_MESSAGE);

        ticketIdInput.setText("");
        refreshViews();
    }

    private void handleFindTicket() {
        String plate = ParkingUtils.normalizePlate(searchPlateInput.getText().trim());
        String ticketInfo = gateSystem.getTicketInfoByPlate(plate);
        if (ticketInfo == null) {
            showError("No active ticket found for this plate.");
            return;
        }

        showInfo(ticketInfo);
    }

    private void refreshViews() {
        statusArea.setText(gateSystem.getStatusReport());
        activeTicketsModel.clear();

        List<Ticket> tickets = gateSystem.getActiveTicketsSnapshot();
        for (Ticket ticket : tickets) {
            String item = String.format(
                    "%s | Slot %d | %s | In: %s",
                    ticket.getTicketID(),
                    ticket.getAssignedSlot() + 1,
                    ticket.getVehicle().getLicensePlate(),
                    ticket.getEntryTimeFormatted()
            );
            activeTicketsModel.addElement(item);
        }
    }

    private JPanel wrapWithTitle(String title, JPanel content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(content, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(280, 180));
        return panel;
    }

    private JPanel wrapWithTitle(String title, JScrollPane content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void launch(int lotSize, boolean testMode) {
        SwingUtilities.invokeLater(() -> {
            ParkingLotGUI gui = new ParkingLotGUI(lotSize, testMode);
            gui.setVisible(true);
        });
    }
}
