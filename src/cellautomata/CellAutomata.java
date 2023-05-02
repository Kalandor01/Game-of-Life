package cellAutomata;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class CellAutomata extends JFrame {
    private static int size = 10;
    private JButton[][] cells = new JButton[size][size];
    private final JButton restartButton = new JButton("New game");
    private final JButton randomPopulationbutton = new JButton("Increase population");
    private final JButton stepButton = new JButton("Step");
    private final JButton startStopButton = new JButton("Start/Stop");
    private final JLabel sizeLabel = new JLabel("Size: ");
    private final JComboBox sizeComboBox = new JComboBox(new String[]{"10*10", "20*20", "30*30", "40*40", "50*50", "100*100"});
    private final JLabel delayLabel = new JLabel("Delay: ");
    private final JComboBox delayComboBox = new JComboBox(new String[]{"0ms!!!", "50ms", "100ms", "500ms", "1000ms"});
    private final JCheckBox extraColorsCheckBox = new JCheckBox("Show extra colors", true);
    private final JPanel gameSpacePanel = new JPanel(new GridLayout(size, size));
    private final Color COLOR_LIVING = Color.ORANGE;
    private final Color COLOR_DYING = Color.LIGHT_GRAY;
    private final Color COLOR_DEAD = Color.WHITE;
    private final Color COLOR_BIRTH = Color.BLUE;
    private final double RANDOM_POPULATION_INCREASE_CHANCE = 0.2;
    
    private boolean showExtraColors = true;
    private boolean isPlaying = false;
    private int delay = 50;

    public CellAutomata() {
        inicialise(size);
    }
    
    private void playLoop()
    {
        try
        {
            while (isPlaying)
            {
                gameOfLifeStep();
                Thread.sleep(delay);
            }
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(CellAutomata.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void meretetAllit(int size) {
        gameSpacePanel.removeAll();
        repaint();
        gameSpacePanel.setLayout(new GridLayout(size, size));
        cells = new JButton[size][size];
        createCells();
        revalidate();
        restart();
    }

    private void inicialise(int size) {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Cell automata");
        setSize(670, 700);
        setLocationRelativeTo(this);
        JPanel basePanel = new JPanel();
        basePanel.add(restartButton);
        basePanel.add(randomPopulationbutton);
        basePanel.add(stepButton);
        basePanel.add(startStopButton);
        basePanel.add(sizeLabel);
        basePanel.add(sizeComboBox);
        basePanel.add(delayLabel);
        basePanel.add(delayComboBox);
        basePanel.add(extraColorsCheckBox);
        restartButton.addActionListener(e -> pressRestartButton());
        stepButton.addActionListener(e -> pressStepButton());
        randomPopulationbutton.addActionListener(e -> pressRandomPopulationButton());
        startStopButton.addActionListener(e -> pressStartStopButton());
        sizeComboBox.addItemListener(e -> selectSize(e));
        delayComboBox.setSelectedIndex(1);
        delayComboBox.addItemListener(e -> selectDelay(e));
        extraColorsCheckBox.addActionListener(e -> toggleExtraColors(e));
        add(basePanel, BorderLayout.NORTH);
        createCells();
        add(gameSpacePanel);
        setVisible(true);
        restart();
    }
    
    private void pressRestartButton()
    {
        size = getSize(sizeComboBox);
        delay = getDelay(delayComboBox);
        meretetAllit(size);
    }
    
    private static int getSize(JComboBox comboBox)
    {
        return Integer.parseInt(comboBox.getSelectedItem().toString().split("\\*")[0]);
    }
    
    private static int getDelay(JComboBox comboBox)
    {
        return Integer.parseInt(comboBox.getSelectedItem().toString().split("ms")[0]);
    }
    
    private void pressRandomPopulationButton()
    {
        for (JButton[] row : cells)
        {
            for (JButton cell : row)
            {
                if (Math.random() < RANDOM_POPULATION_INCREASE_CHANCE)
                {
                    var bg = cell.getBackground();
                    if (bg == COLOR_DEAD || bg == COLOR_BIRTH)
                    {
                        cell.setBackground(COLOR_LIVING);
                    }
                }
            }
        }
        overlayExtraColors();
    }
    
    private void pressStepButton()
    {
        gameOfLifeStep();
    }
    
    private void pressStartStopButton()
    {
        isPlaying = !isPlaying;
        if (isPlaying)
        {
            var loopWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    playLoop();
                    return null;
                }
            };
            loopWorker.execute();
        }
    }
    
    private void pressCellButton(ActionEvent e)
    {
        JButton cell = (JButton) e.getSource();
        var bg = cell.getBackground();
        // user click dead cell
        if (bg == COLOR_DEAD || bg == COLOR_BIRTH)
        {
            cell.setBackground(COLOR_LIVING);
        }
        // user click living cell
        else
        {
            cell.setBackground(COLOR_DEAD);
        }
        overlayExtraColors();
    }
    
    private void selectSize(ItemEvent e)
    {
        var sizeBox = (JComboBox) e.getSource();
        var newSize = getSize(sizeBox);
        if (size != newSize)
        {
            size = newSize;
            meretetAllit(size);
        }
    }
    
    private void selectDelay(ItemEvent e)
    {
        var delayBox = (JComboBox) e.getSource();
        var newdelay = getDelay(delayBox);
        if (delay != newdelay)
        {
            delay = newdelay;
        }
    }

    private void createCells() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new JButton();
                cells[i][j].addActionListener(e -> pressCellButton(e));
                gameSpacePanel.add(cells[i][j]);
            }
        }
    }

    private void restart() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j].setBackground(COLOR_DEAD);
            }
        }
    }

    public void gameOfLifeStep() {
        var newState = new boolean[size][size];
        for (int x = 0; x < size; x++)
        {
            for (int y = 0; y < size; y++)
            {
                newState[x][y] = isAlive(isLiving(x, y) == 1, countNeighbours(x, y));
            }
        }
        for (int x = 0; x < size; x++)
        {
            for (int y = 0; y < size; y++)
            {
                cells[x][y].setBackground(newState[x][y] ? COLOR_LIVING : COLOR_DEAD);
            }
        }
        if (showExtraColors)
        {
            overlayExtraColors();
        }
    }
    
    private void toggleExtraColors(ActionEvent e)
    {
        var toggle = (JCheckBox) e.getSource();
        showExtraColors = toggle.isSelected();
        if (showExtraColors)
        {
            overlayExtraColors();
        }
        else
        {
            hideExtraColors();
        }
    }
    
    private void hideExtraColors()
    {
        for (JButton[] row : cells)
        {
            for (JButton cell : row)
            {
                var bg = cell.getBackground();
                if (bg == COLOR_BIRTH)
                {
                    cell.setBackground(COLOR_DEAD);
                }
                else if (bg == COLOR_DYING)
                {
                    cell.setBackground(COLOR_LIVING);
                }
            }
        }
    }
    
    private void overlayExtraColors()
    {
        var newerState = new boolean[size][size];
        for (int x = 0; x < size; x++)
        {
            for (int y = 0; y < size; y++)
            {
                newerState[x][y] = isAlive(isLiving(x, y) == 1, countNeighbours(x, y));
            }
        }
        for (int x = 0; x < size; x++)
        {
            for (int y = 0; y < size; y++)
            {
                var presentState = isLiving(x, y) == 1;
                var futureState = newerState[x][y];
                if (presentState == futureState)
                {
                    cells[x][y].setBackground(futureState ? COLOR_LIVING : COLOR_DEAD);
                }
                else
                {
                    cells[x][y].setBackground(futureState ? COLOR_BIRTH : COLOR_DYING);
                }
            }
        }
    }
    
    private boolean isAlive(boolean isLiving, int neighbours)
    {
        if (neighbours < 2 || neighbours > 3)
        {
            return false;
        }
        else if (neighbours == 3)
        {
            return true;
        }
        else
        {
            return isLiving;
        }
    }
    
    private int countNeighbours(int x, int y)
    {
        var osszes = 0;
        osszes += isLiving(modulo((x + 1), size), modulo(y, size));
        osszes += isLiving(modulo((x - 1), size), modulo(y, size));
        osszes += isLiving(modulo(x, size), modulo((y + 1), size));
        osszes += isLiving(modulo(x, size), modulo((y - 1), size));
        osszes += isLiving(modulo((x + 1), size), modulo((y + 1), size));
        osszes += isLiving(modulo((x - 1), size), modulo((y - 1), size));
        osszes += isLiving(modulo((x + 1), size), modulo((y - 1), size));
        osszes += isLiving(modulo((x - 1), size), modulo((y + 1), size));
        return osszes;
    }
    
    private int isLiving(int x, int y)
    {
        var cellColor = cells[x][y].getBackground();
        return cellColor == COLOR_LIVING || cellColor == COLOR_DYING ? 1 : 0;
    }
    
    private static int modulo(int n, int mod)
    {
        return ((n % mod) + mod) % mod;
    }
}
