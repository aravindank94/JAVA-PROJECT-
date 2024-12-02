import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class HauntedHouseGame extends JFrame {
    private JTextArea output;
    private JButton saveButton, loadButton;
    private JButton northButton, southButton, eastButton, westButton, takeButton, fightButton;

    private HashMap<String, Room> rooms = new HashMap<>();
    private HashSet<String> inventory = new HashSet<>();
    private Room currentRoom;

    public HauntedHouseGame() {
        setupGUI();
        setupGame();
        showCurrentRoom();
    }

    private void setupGUI() {
        setTitle("Haunted House Adventure");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        output = new JTextArea(10, 30);
        output.setEditable(false);
        output.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(output);

        // Save and Load buttons
        saveButton = new JButton("Save");
        saveButton.addActionListener(new SaveHandler());

        loadButton = new JButton("Load");
        loadButton.addActionListener(new LoadHandler());

        // Directional movement buttons
        northButton = new JButton("North");
        northButton.addActionListener(e -> move("north"));

        southButton = new JButton("South");
        southButton.addActionListener(e -> move("south"));

        eastButton = new JButton("East");
        eastButton.addActionListener(e -> move("east"));

        westButton = new JButton("West");
        westButton.addActionListener(e -> move("west"));

        // Take button to pick up items
        takeButton = new JButton("Take");
        takeButton.addActionListener(e -> takeItem());

        // Fight button for combat
        fightButton = new JButton("Fight");
        fightButton.addActionListener(e -> fightMonster());

        // Layout the components
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        buttonPanel.add(saveButton);
        buttonPanel.add(northButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(fightButton);
        buttonPanel.add(westButton);
        buttonPanel.add(southButton);
        buttonPanel.add(eastButton);
        buttonPanel.add(takeButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void setupGame() {
        // Define rooms
        Room foyer = new Room("Foyer", "You are in the foyer. A grand chandelier hangs above.");
        Room kitchen = new Room("Kitchen", "You see a dusty kitchen with old pots and pans.");
        Room library = new Room("Library", "Shelves of old books surround you. There's a strange silence.");
        Room mysteryRoom = new Room("Mystery Room", "This room has an eerie feeling and a locked door with strange symbols.");
        Room basement = new Room("Basement", "A dark, damp room with chains hanging from the walls.");

        // Link rooms
        foyer.setExit("north", library);
        foyer.setExit("east", kitchen);
        kitchen.setExit("west", foyer);
        library.setExit("south", foyer);
        library.setExit("east", mysteryRoom);

        mysteryRoom.setExit("down", basement);

        // Add items (weapons, keys, spells)
        kitchen.setItem("key");
        library.setItem("sword");
        basement.setItem("spell");

        // Add monsters
        mysteryRoom.setMonster(new Monster("Ghost", "A terrifying ghost that haunts the room."));

        rooms.put("foyer", foyer);
        rooms.put("kitchen", kitchen);
        rooms.put("library", library);
        rooms.put("mysteryRoom", mysteryRoom);
        rooms.put("basement", basement);

        currentRoom = foyer;
    }

    private void showCurrentRoom() {
        output.setText(currentRoom.getDescription() + "\nExits: " + currentRoom.getExits() + "\n");

        if (currentRoom.getItem() != null) {
            output.append("You see a " + currentRoom.getItem() + " here.\n");
        }
        if (currentRoom.getMonster() != null) {
            output.append("Beware! " + currentRoom.getMonster().getDescription() + "\n");
        }

        output.append("Inventory: " + inventory + "\n");
    }

    private void move(String direction) {
        Room nextRoom = currentRoom.getExit(direction);
        if (nextRoom == null) {
            output.append("You can't go that way!\n");
        } else if (nextRoom == rooms.get("mysteryRoom") && !inventory.contains("key")) {
            output.append("The door is locked. You need a key.\n");
        } else {
            currentRoom = nextRoom;
            showCurrentRoom();
        }
    }

    private void takeItem() {
        if (currentRoom.getItem() != null) {
            inventory.add(currentRoom.getItem());
            output.append("You picked up: " + currentRoom.getItem() + "\n");
            currentRoom.setItem(null);
        } else {
            output.append("There's nothing to take here.\n");
        }
    }

    private void fightMonster() {
        Monster monster = currentRoom.getMonster();
        if (monster == null) {
            output.append("There is no monster here to fight.\n");
            return;
        }

        if (inventory.contains("sword") || inventory.contains("spell")) {
            output.append("You defeated the " + monster.getName() + " with your weapon!\n");
            currentRoom.setMonster(null);
        } else {
            output.append("You have no weapon or spell to fight the " + monster.getName() + "!\n");
        }
    }

    private class SaveHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("savegame.dat"))) {
                out.writeObject(currentRoom.getName());
                out.writeObject(inventory);
                output.append("Game saved.\n");
            } catch (IOException ex) {
                output.append("Error saving game.\n");
            }
        }
    }

    private class LoadHandler implements ActionListener {
        @SuppressWarnings("unchecked")
        @Override
        public void actionPerformed(ActionEvent e) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("savegame.dat"))) {
                String roomName = (String) in.readObject();
                inventory = (HashSet<String>) in.readObject();
                currentRoom = rooms.get(roomName);
                showCurrentRoom();
                output.append("Game loaded.\n");
            } catch (IOException | ClassNotFoundException ex) {
                output.append("Error loading game.\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HauntedHouseGame::new);
    }

    private static class Room implements Serializable {
        private String name;
        private String description;
        private HashMap<String, Room> exits = new HashMap<>();
        private String item;
        private Monster monster;

        public Room(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public void setExit(String direction, Room room) {
            exits.put(direction, room);
        }

        public Room getExit(String direction) {
            return exits.get(direction);
        }

        public String getExits() {
            return String.join(", ", exits.keySet());
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public Monster getMonster() {
            return monster;
        }

        public void setMonster(Monster monster) {
            this.monster = monster;
        }
    }

    private static class Monster implements Serializable {
        private String name;
        private String description;

        public Monster(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
