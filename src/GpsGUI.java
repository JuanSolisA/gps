import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.*;

public class GpsGUI extends JFrame {
    private JTextField persona1Field;
    private JTextField persona2Field;
    private JLabel resultadoLabel;
    private Connection connection;

    // Constructor
    public GpsGUI() {
        // Configuración básica de la GUI
        setTitle("Contador de cruces de calle");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel persona1Label = new JLabel("Persona 1:");
        persona1Label.setBounds(50, 50, 100, 25);
        add(persona1Label);

        persona1Field = new JTextField();
        persona1Field.setBounds(150, 50, 150, 25);
        add(persona1Field);

        JLabel persona2Label = new JLabel("Persona 2:");
        persona2Label.setBounds(50, 100, 100, 25);
        add(persona2Label);

        persona2Field = new JTextField();
        persona2Field.setBounds(150, 100, 150, 25);
        add(persona2Field);

        JButton calcularButton = new JButton("Calcular Grados");
        calcularButton.setBounds(100, 150, 150, 30);
        add(calcularButton);

        resultadoLabel = new JLabel("Grado de separación: ");
        resultadoLabel.setBounds(50, 200, 300, 25);
        add(resultadoLabel);

        // Acción del botón para calcular el grado de separación
        calcularButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String persona1 = persona1Field.getText();
                String persona2 = persona2Field.getText();
                try {
                    int grados = calcularGradoDeSeparacion(persona1, persona2);
                    resultadoLabel.setText("Grado de separación: " + grados);
                } catch (Exception ex) {
                    resultadoLabel.setText("Error: " + ex.getMessage());
                }
            }
        });

        // Conectar a la base de datos
        conectarBaseDeDatos();
    }

    // Método para conectar a la base de datos
    private void conectarBaseDeDatos() {
        try {
            String url = "jdbc:mysql://localhost:3306/seis_grados";
            String user = "root";
            String password = "";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para calcular el grado de separación usando BFS
    public int calcularGradoDeSeparacion(String persona1, String persona2) throws SQLException {
        if (persona1.equals(persona2)) {
            return 0; // Si las personas son las mismas, el grado es 0
        }

        // Mapa para almacenar el grado de separación de cada persona
        Map<String, Integer> grados = new HashMap<>();
        Queue<String> queue = new LinkedList<>();

        // Iniciar la búsqueda desde la primera persona
        queue.add(persona1);
        grados.put(persona1, 0);

        // Algoritmo de búsqueda en anchura (BFS)
        while (!queue.isEmpty()) {
            String actual = queue.poll();
            int gradoActual = grados.get(actual);

            // Obtener los conocidos de la persona actual
            List<String> conocidos = getConocidos(actual);

            for (String conocido : conocidos) {
                if (!grados.containsKey(conocido)) {
                    grados.put(conocido, gradoActual + 1);
                    queue.add(conocido);

                    // Si encontramos a la segunda persona, devolvemos el grado
                    if (conocido.equals(persona2)) {
                        return grados.get(conocido);
                    }
                }
            }
        }

        // Si no se encuentra una conexión, devolvemos -1
        return -1;
    }

    // Método para obtener las personas conocidas de una persona desde la base de datos
    public List<String> getConocidos(String nombre) throws SQLException {
        List<String> conocidos = new ArrayList<>();
        
        // Consulta SQL que busca conocidos en ambas direcciones
        String query = "SELECT conoce FROM conocidos WHERE nombre = ? UNION SELECT nombre FROM conocidos WHERE conoce = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nombre);
            stmt.setString(2, nombre);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                conocidos.add(rs.getString(1));  // Usamos el índice 1 ya que ambas columnas tienen el mismo nombre
            }
        }

        return conocidos;
    }

    // Main para ejecutar la aplicación
    //public static void main(String[] args) {
    //    SwingUtilities.invokeLater(() -> {
    //        GpsGUI gui = new GpsGUI();
    //        gui.setVisible(true);
    //    });
    //}
}
