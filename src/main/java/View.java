import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import org.json.JSONObject;

public class View extends JFrame {

    private static final int ICON_SIZE = 80;
    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 15);
    public JSONObject json = new JSONObject(readApi());
    JLabel logo, link, variacion, compra, venta, fecha;

    public View() throws IOException, InterruptedException {
        setTitle("DolarHoy");
        setSize(330, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 3));
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/os.png"))).getImage());

        logo = createImageLabel();
        link = createLinkLabel();
        variacion = createIconLabel();

        compra = createHtmlLabel("compra");
        venta = createHtmlLabel("venta");
        fecha = createHtmlLabel("fecha");

        compra.setName("compra");
        venta.setName("venta");
        fecha.setName("fecha");


        add(logo);
        add(link);
        add(variacion);
        add(compra);
        add(venta);
        add(fecha);
    }

    private void updateJsonAndUI() throws IOException, InterruptedException {
        json = new JSONObject(readApi());

        updateIconLabel(variacion);
        updateHtmlLabel(compra);
        updateHtmlLabel(venta);
        updateHtmlLabel(fecha);
    }


    private JLabel createImageLabel() {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/os.png")));
        Image resizedImage = icon.getImage().getScaledInstance(View.ICON_SIZE, View.ICON_SIZE, Image.SCALE_SMOOTH);
        JLabel label = new JLabel(new ImageIcon(resizedImage));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(createMouseListenerForImage());
        return label;
    }

    private JLabel createLinkLabel() {
        JLabel label = new JLabel("Dolar Blue");
        label.addMouseListener(createMouseListener());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setFont(new Font("Arial", Font.BOLD, 20));
        return label;
    }

    private JLabel createIconLabel() {
        String iconPath = json.getString("class-variacion").equals("up") ? "/Chevron Up.png" : "/Chevron Down.png";
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(iconPath)));
        String text = json.getString("variacion");
        JLabel label = new JLabel(text, icon, SwingConstants.CENTER);
        label.setForeground(json.getString("class-variacion").equals("up") ? Color.GREEN : Color.RED);
        label.setFont(new Font("Arial", Font.BOLD, 25));
        return label;
    }

    private void updateIconLabel(JLabel label){
        String iconPath = json.getString("class-variacion").equals("up") ? "/Chevron Up.png" : "/Chevron Down.png";
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(iconPath)));
        label.setIcon(icon);
        label.setForeground(json.getString("class-variacion").equals("up") ? Color.GREEN : Color.RED);
    }

    private JLabel createHtmlLabel(String name) {
        JLabel label;
        if (name.equals("fecha")) {
            String fechaFormat = json.getString(name);
            fechaFormat = fechaFormat.replace("-","<br>");
            label = new JLabel("<html><center>" + fechaFormat + "</center></html>");
        } else {
            label = new JLabel("<html><center>" + name.toUpperCase() + "<br>" + json.getString(name) + "</center></html>");
        }
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(LABEL_FONT);
        return label;
    }

    private void updateHtmlLabel(JLabel label){
        if (label.getName().equals("fecha")) {
            String fechaFormat = json.getString(label.getName());
            fechaFormat = fechaFormat.replace("-","<br>");
            label.setText("<html><center>" + fechaFormat + "</center></html>");
        } else {
            label.setText("<html><center>" + label.getName().toUpperCase() + "<br>" + json.getString(label.getName()) + "</center></html>");
        }
    }

    private MouseAdapter createMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.ambito.com/contenidos/dolar-informal.html"));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace(System.out);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (e.getSource() instanceof JLabel label) {

                    label.setForeground(Color.BLUE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (e.getSource() instanceof JLabel label) {

                    label.setForeground(Color.BLACK);
                }
            }
        };
    }
    private MouseAdapter createMouseListenerForImage() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    System.out.println("Actualizado");
                    updateJsonAndUI();
                } catch (IOException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }
    private String readApi() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        String apiUrl = "https://mercados.ambito.com//dolar/informal/variacion";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new View().setVisible(true);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
