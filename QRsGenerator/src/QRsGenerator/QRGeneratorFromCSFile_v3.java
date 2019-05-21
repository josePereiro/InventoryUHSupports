package QRsGenerator;

import CSVFile.CSVFile;
import MyJavaTools.FileTools;
import MyJavaTools.ImageTools;
import MyQRTools.QRCodeTools;
import com.google.zxing.WriterException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class QRGeneratorFromCSFile_v3 {

    //Fields
    private static final java.util.Scanner scanner = new java.util.Scanner(System.in);
    private static CSVFile.InventoryUH.Reader csvFileReader;
    private static ArrayList<ArrayList<String>> data;
    private static ArrayList<String> areas;
    private static Point[] formats = new Point[]{
            new Point(1, 1),
            new Point(5, 7),
            new Point(8, 10)
    };
    private static File csvFile;

    public static void main(String... args) {

        //Info
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!!!!!!!!!!!     GENERADOR DE QRs      !!!!!!!!!!!!");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.print("Precione Enter para abrir el explorador!!!");
        scanner.nextLine();
        scanner.reset();

        //AskForCsv
        if (!askForCSVFile()) return;

        //Getting Data
        getData();

        //Info
        System.out.print("Precione Enter para comenzar a generar los QRs!!!");
        scanner.nextLine();
        scanner.reset();

        //Generating QRs
        try {
            generateQrs();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void generateQrs() throws Exception {

        for (Point format : formats) {
            int formatColumnCount = format.x;
            int formatRawCount = format.y;
            int totalNumbersToEncode = formatColumnCount * formatRawCount;
            String formatName = "_" + formatColumnCount + "x" + formatRawCount;

            //Info
            System.out.println();
            System.out.println("Formato " + formatName);

            ArrayList<String> numbersOfThisArea;
            ArrayList<String> numbersToEncode;
            for (int areaIndex = 0; areaIndex < data.size(); areaIndex++) {
                numbersOfThisArea = data.get(areaIndex);

                //Info
                System.out.println("Área " + areas.get(areaIndex) + " números " + data.get(areaIndex).size());

                for (int currentNumberIndex = 0; currentNumberIndex < numbersOfThisArea.size(); currentNumberIndex += totalNumbersToEncode) {
                    if (currentNumberIndex + totalNumbersToEncode < numbersOfThisArea.size()) {
                        numbersToEncode = new ArrayList<>(numbersOfThisArea.subList(currentNumberIndex, currentNumberIndex + totalNumbersToEncode));
                    } else {
                        numbersToEncode = new ArrayList<>(numbersOfThisArea.subList(currentNumberIndex, numbersOfThisArea.size()));
                    }

                    //Info
                    System.out.println(currentNumberIndex + ": codificando " + numbersToEncode.size() + " números...");

                    //GeneratingImage
                    ArrayList<BufferedImage> qrs = new ArrayList<>();
                    for (String numberToEncode : numbersToEncode) {
                        try {
                            qrs.add(QRCodeTools.getLabeledQRImage(numberToEncode));
                        } catch (WriterException e) {
                            System.out.println("ERROR FATAL: Por alguna razon el qr no pudo ser creado!!!");
                            scanner.nextLine();
                            return;
                        }
                    }

                    //Generating grid
                    BufferedImage grid = ImageTools.
                            addImagesAsAGrid(qrs, formatColumnCount, formatRawCount,
                                    qrs.get(0).getWidth(), qrs.get(0).getHeight(), 3, Color.WHITE);

                    //RootFile
                    File rootFile = new File(csvFile.getParent(), csvFile.getName().replaceAll(".csv", ""));
                    if (!createDirAndCheck(rootFile)) return;

                    //AreaFile
                    File areaFile = new File(rootFile,
                            FileTools.
                                    formatStringForFileName(areas.get(areaIndex)) + "_" + data.get(areaIndex).size());
                    if (!createDirAndCheck(areaFile)) return;

                    //FormatFile
                    File formatFile = new File(areaFile, formatName);
                    if (!createDirAndCheck(formatFile)) return;

                    //ImageFile
                    try {
                        String imageName = currentNumberIndex + "_" + data.get(areaIndex).get(currentNumberIndex) + "_" +
                                numbersToEncode.size() + ".jpg";
                        File imageFile = new File(formatFile, imageName);
                        ImageIO.write(grid, "jpg", imageFile);

                        //Info
                        System.out.println("Imagen creada en " + imageFile.getAbsolutePath());

                    } catch (IOException e) {
                        System.out.println("ERROR FATAL: no se pudo guardar la imagen!!!");
                        scanner.nextLine();
                        return;
                    }


                }

            }

        }

    }

    private static boolean createDirAndCheck(File file) {

        if (!file.exists()) {
            if (!file.mkdirs() || !file.isDirectory()) {
                System.out.println("ERROR FATAL: no se pudo crear la carpeta " + file.getAbsolutePath());
                return false;
            }
        }
        return true;
    }

    private static void loadTestFile() {
        try {
            csvFile = new File("/Users/Pereiro/Documents/Inventory_UH_Test_File_Tue_Jan_01_16_10_25_EST_2019.csv");
            csvFileReader = new CSVFile.InventoryUH.Reader(csvFile);
            System.out.println("Archivo cargado y compatible, " + (csvFileReader.getLineCount() - 1) + " números detectados!!!");
        } catch (IOException | CSVFile.CorruptedFileException | CSVFile.BrokenDataException ignored) {
            ignored.printStackTrace();
        }
    }

    private static boolean askForCSVFile() {

        //Ask for csv file
        csvFile = FileTools.openFileDialog("Escoja un archivo csv!!!");
        if (csvFile != null) {
            System.out.println("Archivo Selecionado: " + csvFile.getPath());
            try {
                csvFileReader = new CSVFile.InventoryUH.Reader(csvFile);
                System.out.println("Archivo cargado y compatible, " + (csvFileReader.getLineCount() - 1) + " números detectados!!!");
            } catch (IOException e) {
                System.out.println("ERROR: El archivo no se pudo leer!!!");
                scanner.nextLine();
                return false;
            } catch (CSVFile.CorruptedFileException e) {
                System.out.println("ERROR: El archivo esta corrupto o lesionado!!!");
                scanner.nextLine();
                return false;
            } catch (Exception ignored) {
                System.out.println("ERROR INESPERADO: Algo se chivó y no sabemos que fue!!!");
                scanner.nextLine();
                return false;
            }
        } else {
            System.out.println("Operación cancelada!!!");
            scanner.nextLine();
            return false;
        }

        return true;
    }

    private static void getData() {

        areas = new ArrayList<>();
        ArrayList<String> allAreas = csvFileReader.getAreas();
        for (String area : allAreas) {
            if (!areas.contains(area)) {
                areas.add(area);
            }
        }

        data = new ArrayList<>();
        ArrayList<String> areaNumbers;
        ArrayList<String> numbers = csvFileReader.getNumbers();
        for (String area : areas) {
            areaNumbers = new ArrayList<>();
            for (int i = 0; i < allAreas.size(); i++) {
                if (allAreas.get(i).equals(area)) {
                    areaNumbers.add(numbers.get(i));
                }
            }
            data.add(areaNumbers);
        }


    }


}
