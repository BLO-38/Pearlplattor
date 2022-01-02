import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.Arrays;

public class Pearlplatts {



        // Ändra så att dimensionsförändring behåller pärlantalet
        private int height, width;
        private final int WINDOW_WIDTH = 800;
        private int[] rgbValues, brightnesses, numbers;
        boolean isAutomatic = false;
        boolean isManual = false;
        boolean showOnce = false;
        int mosaicColumns = 1;
        private boolean programSetValue = false;
        final int INITIAL_PEARLS = 20;
        final int MAX_PEARLS = 5;
        int [] grayLevels = {50,100,150,200};
        int controlSum = 500;
        JLabel[] sliderTexts = new JLabel[MAX_PEARLS-1];
        JSlider[] graySliders = new JSlider[MAX_PEARLS-1];
        JFrame frame = new JFrame();
        JPanel mosaicPanel = new JPanel();
        JPanel sliderPanel = new JPanel();
        JLabel dimensionsInfoText = new JLabel();
        JLabel pearlsInfoText = new JLabel("Antal pärlfärger: " + (INITIAL_PEARLS+1));
        JLabel dimenstioSliderText = new JLabel("Ändra dimensioner");
        JLabel pearlsSliderText = new JLabel("Ändra antal pärlfärger");
        JLabel slidersHeader = new JLabel("Ändra gråskala:");
        JSlider pearlsSlider, dimensionSlider;
        JButton showNumbersButton;

        public Pearlplatts() {
            System.out.println("Start");
            frame.setLayout(new FlowLayout());
            sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
            frame.add(sliderPanel);
            frame.add(mosaicPanel);
            dimensionsInfoText.setFont(new Font(null, Font.PLAIN,25));
            pearlsInfoText.setFont(new Font(null, Font.PLAIN,20));
            setRgbValues();
            String[] showModes = {"En visning","Automatisk ökning","Justera manuellt"};
            int result = JOptionPane.showOptionDialog(null,
                "Välj typ av bildvisning",
                null,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                showModes,
                null);
            if(result == 0) {
                while(true) {
                    showOnce = true;
                    int chosenColumns = Integer.parseInt(JOptionPane.showInputDialog("Ange antal pärtlor på bredden:"));
                    calculateBrightnesses(chosenColumns);
                    //reduceGrayScale(5);
                    showMosaic(brightnesses);
                }
            }
            else if (result == 1) {
                isAutomatic = true;
                mosaicColumns = 1;
                for (int size = 3 ; size < 100 ; size++) {
                    if(calculateBrightnesses(size)) {
                        //reduceGrayScale(2);
                        showMosaic(brightnesses);
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            System.out.println("Sovtråden");
                            break;
                        }
                    } else
                        System.out.println("Hoppade över " + size);
                }
            }
            else if (result == 2) {
                for(int i=0; i<MAX_PEARLS-1 ; i++) {
                    String text;
                    if (i==0) text = "Gräns för svart";
                    else if (i==MAX_PEARLS-2) text = "Gräns för vitt";
                    else text = "Grånivå nr " + i;
                    JLabel label = new JLabel(text);
                    JSlider slider = new JSlider(JSlider.HORIZONTAL,0,255,50 + 50 * i);
                    sliderTexts[i] = label;
                    graySliders[i] = slider;
                    slider.setName("S" + i);
                    final int i2 = i;
                    slider.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            if(!slider.getValueIsAdjusting() ){//&& !programSetValue) {
                                if(!programSetValue) {
                                    System.out.println("\nGrånivå " + (i2+1) + " ändras.");
                                    System.out.println("Slider nr: " + slider.getName());
                                    boolean checkOk = checkLevel(Integer.parseInt(slider.getName().substring(1)));
                                    if(checkOk) {
                                        System.out.print("Dags att rita bild med nya värden: ");
                                        for (JSlider s : graySliders)
                                            System.out.print(s.getValue() + ", ");
                                        System.out.println();
                                        int[] reducedValues = getReducedBrightnesses();
                                        // showNumberMosaic(numbers);
                                        if(reducedValues != null) showMosaic(reducedValues);
                                        System.out.println("Brightnesses: " + Arrays.toString(brightnesses));
                                        System.out.println("Reduced: " + Arrays.toString(reducedValues));
                                    } else {
                                        System.out.println("Vi gör ingen ny bild.");
                                    }

                                } else
                                    System.out.println("Programmet bytte värde till ." + slider.getValue() + " Ingen check");


                            }

                        }
                    });
                }
                isManual = true;
                showNumbersButton = new JButton("Nummer");
                showNumbersButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showNumberMosaic(numbers);
                    }
                });
                mosaicColumns = 20;
                pearlsSlider = new JSlider(JSlider.HORIZONTAL, 1, INITIAL_PEARLS, INITIAL_PEARLS);
                pearlsSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        if (!pearlsSlider.getValueIsAdjusting()){
                            System.out.println("GLIID på gråskalan" + pearlsSlider.getValue());
                            int newValue = pearlsSlider.getValue();
                            pearlsInfoText.setText("Antal olika pärlfärger: " + (newValue + 1));
                            if(newValue<=MAX_PEARLS) slidersHeader.setForeground(Color.GREEN);

                            int[] reducedValues = getReducedBrightnesses();
                            System.out.println("Här kommer det nya:");
                            System.out.println("Brightnesses: " + Arrays.toString(brightnesses));
                            System.out.println("Reduced: " + Arrays.toString(reducedValues));
                            if(reducedValues != null) showMosaic(reducedValues);
                        }
                    }
                });
                dimensionSlider = new JSlider(JSlider.HORIZONTAL, 5, 100, mosaicColumns);
                dimensionSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        if (!dimensionSlider.getValueIsAdjusting()) {
                            System.out.println("GLIID på mosaikupplösningen" + dimensionSlider.getValue());
                            int columns = dimensionSlider.getValue();
                            if(calculateBrightnesses(columns))
                                showMosaic(brightnesses);
                        }

                    }
                });

                sliderPanel.add(dimensionsInfoText);
                sliderPanel.add(pearlsInfoText);

                sliderPanel.add(dimenstioSliderText);
                sliderPanel.add(dimensionSlider);

                sliderPanel.add(pearlsSliderText);
                sliderPanel.add(pearlsSlider);
                slidersHeader.setForeground(Color.RED);
                sliderPanel.add(slidersHeader);

                for (int i=0; i<MAX_PEARLS-1; i++) {
                    sliderPanel.add(sliderTexts[i]);
                    sliderPanel.add(graySliders[i]);
                }
                sliderPanel.add(showNumbersButton);
                calculateBrightnesses(mosaicColumns);
                // reduceGrayScale(INITIAL_PEARLS, 0);
                showMosaic(brightnesses);
            }
            else System.exit(0);
            // setRandomBrightnesses();

        }
        public static void main(String[] args) {
            new Pearlplatts();
        }

        private void showMosaic(int[] brightnessesToShow) {
            frame.remove(mosaicPanel);
            mosaicPanel = new JPanel();
            int panelSize = WINDOW_WIDTH/mosaicColumns;
            int mosaicRows = brightnessesToShow.length/mosaicColumns;
            mosaicPanel.setLayout(new GridLayout(mosaicRows , mosaicColumns));

            System.out.println("Mosaik dimensioner: " + mosaicRows + " : " + mosaicColumns);
            dimensionsInfoText.setText("Dimensioner: " + mosaicRows + " x " + mosaicColumns);

            for(int bright : brightnessesToShow) {
                JPanel p = new JPanel();
                p.setBackground(new Color(bright, bright, bright));
                p.setPreferredSize(new Dimension(panelSize,panelSize));
                mosaicPanel.add(p);
            }
            frame.add(mosaicPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
        }
        private void showNumberMosaic(int[] numbersToShow) {
            frame.remove(mosaicPanel);
            mosaicPanel = new JPanel();
            int panelSize = WINDOW_WIDTH/mosaicColumns;
            int mosaicRows = numbersToShow.length/mosaicColumns;
            mosaicPanel.setLayout(new GridLayout(mosaicRows , mosaicColumns));

            System.out.println("Mosaik dimensioner: " + mosaicRows + " : " + mosaicColumns);
            dimensionsInfoText.setText("Dimensioner: " + mosaicRows + " x " + mosaicColumns);

            for(int bright : numbersToShow) {
                JPanel p = new JPanel();
                p.setBorder(new LineBorder(Color.BLUE,1));
                p.setBackground(new Color(200 ,200,200));
                p.setPreferredSize(new Dimension(25,25));
                // p.setPreferredSize(new Dimension(panelSize,panelSize));
                p.add(new JLabel(String.valueOf(bright)));
                mosaicPanel.add(p);
            }
            frame.add(mosaicPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
        }
        private void setRgbValues() {
            String fileName = JOptionPane.showInputDialog("Skriv filnamn utan filändelse.");
            File picFile =  new File("Bilder/" + fileName + ".jpg");
            if(!picFile.exists()) {
                JOptionPane.showMessageDialog(null, "Filen hittades inte");
                System.exit(0);
            }
            try {
                BufferedImage bilden = ImageIO.read(picFile);
                height = bilden.getHeight();
                width = bilden.getWidth();
                System.out.println("Höjd: " + height);
                System.out.println("Bredd: " + width);

                byte[] data = ((DataBufferByte) bilden.getRaster().getDataBuffer()).getData();
                System.out.println("Datalength: " + data.length );
                int startval = 95/2*3*629;

                System.out.println("Bilddata:");
                for(int j=startval; j<startval+100; j++) {
                    System.out.print(data[j] + " ");
                }

                rgbValues = new int[data.length];

                for(int i=0;i<data.length;i++) {
                    int p = data[i];
                    if (p < 0) p = 256 + p;
                    rgbValues[i] = p;
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        private void setRandomBrightnesses() {
            int[] randomInts = new int[100];
            for(int i=0; i<randomInts.length; i++) {
                int ljusst = (int) (Math.random()*255);
                randomInts[i] = ljusst;
            }
            brightnesses = randomInts;
        }
        private boolean checkLevel(int sliderNr) {
            int currentValue = graySliders[sliderNr].getValue();
            System.out.println("Checkar nr " + sliderNr + " med nytt värde: " + currentValue);

            int lowLimit = sliderNr == 0 ? 1 : graySliders[sliderNr-1].getValue();
            int highLimit = sliderNr == MAX_PEARLS-2 ? 254 : graySliders[sliderNr+1].getValue();
            System.out.println("Low limit: " + lowLimit);
            System.out.println("High limit: " + highLimit);

            if(currentValue <= lowLimit || currentValue >= highLimit) {
                programSetValue = true;
                int newValue = currentValue >= highLimit ? highLimit-1 : lowLimit+1;
                graySliders[sliderNr].setValue(newValue);
                EventQueue.invokeLater(() -> {
                    programSetValue = false;
                });
                System.out.println("Tvångsbestämde värdet");

            }
            int temSum = 0;
            int[] levels = new int[4];
            for(int i=0; i<MAX_PEARLS-1; i++) {
                int val = graySliders[i].getValue();
                levels[i] = val;
                temSum += val;
            }
            if(temSum != controlSum) {
                System.out.println("Nya värden!!");
                controlSum = temSum;
                grayLevels = levels;
                return true;
            }
            System.out.println("Samma summa, inget ändras.");
            return false;

        }

        private int[] getReducedBrightnesses () {

            int antalPearls = pearlsSlider.getValue();
            if (antalPearls < 1 || antalPearls > 200) {
                System.out.println("Det är " + antalPearls + " pärlor så vi fixar ingen ny gråskala");
                return null;
            }
            int[] newValues = new int[brightnesses.length];
            numbers = new int[brightnesses.length];
            int[] pearlBrightnesses = {0,64,128,192,255};
            if(antalPearls <= 5) {
                System.out.println("Vi fixar med slidernas gråskala");
                for (int i = 0 ; i < brightnesses.length ; i++) {
                    for (int j=0; j<MAX_PEARLS-1 ; j++) {
                        if (brightnesses[i] < grayLevels[j]) {
                            newValues[i] = pearlBrightnesses[j];
                            numbers[i] = j;
                            break;
                        }
                        newValues[i] = pearlBrightnesses[4];
                        numbers[i] = 4;
                    }
                }
            } else {
                System.out.println("Vi gör en jämn gråfördelning med måånga grånyanser");
                int divisor = 255 / antalPearls;
                System.out.println("Vi delar med " + divisor + ", antal gråfärger: " + antalPearls);
                for (int i = 0; i < brightnesses.length; i++) {
                    int brightness = brightnesses[i] / divisor;
                    // System.out.println("Brightness " + brightness);
                    newValues[i] = brightness * divisor;
                }
            }
            System.out.println("Med siffror: " + Arrays.toString(numbers));
            return newValues;
        }

        private boolean calculateBrightnesses(int size) {
            System.out.println("CALCULATE --------------========================>>>>>>>>>>>>>>>>>>>>");
            int sidlangd = width/size;
            int newMosaicColumns = width/sidlangd;
            if (brightnesses != null && newMosaicColumns == mosaicColumns) {
                System.out.println("Det blev samma antal pärlor när vi tog size = " + size + ". Nämligen " + mosaicColumns);
                return false;
            } else {
                System.out.println("Det blev nytt antal! size: " + size + " , nya columns: " + newMosaicColumns + ", gamla mosaiccolumns: " + mosaicColumns);
                mosaicColumns = newMosaicColumns;
            }
            System.out.println("##########  NY CALC");
            System.out.println("Rutor i x: " + mosaicColumns);
            System.out.println("Sidlängd: " + sidlangd);
            System.out.println("Justerat rutor i x: " + mosaicColumns);
            int lodratarutor = height/sidlangd;
            System.out.println("Lodrätt: " + lodratarutor);
            int total = mosaicColumns*lodratarutor;
            System.out.println("Totalt antal rutor: " + total);
            brightnesses = new int[total];

            int index = 0;
            int rowDatalength = width*3;
            System.out.println("Raddata " + rowDatalength);
            int rgbArrIndex = 0;

            for(int row=0 ; row<lodratarutor; row++) {
                int rowStartindex = row*rowDatalength*sidlangd + (sidlangd/2)*rowDatalength;
                for(int column = 0 ; column<mosaicColumns; column++ ) {
                    rgbArrIndex = rowStartindex + column*sidlangd*3;
                    brightnesses[index] = rgbValues[rgbArrIndex];
                    index++;
                }
            }
            System.out.println(total + " ::: " + index);
            System.out.println(rgbValues.length + " --> " + rgbArrIndex);
            return true;
        }
    }




