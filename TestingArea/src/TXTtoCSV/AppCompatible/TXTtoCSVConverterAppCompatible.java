package TXTtoCSV.AppCompatible;

import java.io.*;
import java.util.ArrayList;

/**
 * This program convert a inventory txt file into a csv file. The txt most be obtained from the inventory PDF
 * using the program Foxit Reader 6.1.1.1025
 * The format of the csv file is the next:
 * First Line:
 * First element: we have a head that is require to identified that this is an uh inventory file, it most be app compatible,
 * otherwise the app will refuse to import the file.
 * Second element: a hashcode calculated from the data content of the csv file. First we convert to a single String all
 * the data of the file, all the lines except the first one, next we use the hashcode method of the myStringHashCode to
 * get the code. This is important, the application will do the same operation and if the hash codes doesn't match it will
 * refuse to import this file!!!
 * Ex of first line: Archivo UH para importar..., 1099990638
 * <p>
 * Others Lines: The data have this order:
 * number, description, area, altaDate, officialUpdate
 * Ex of data line: 0321824, MESA DE FORMICA, ADMINISTRACION LOCAL 284/285 , 31/12/1998, 23/03/2005
 */
public class TXTtoCSVConverterAppCompatible {

    private static String number, area, description, altaDate, updateDate;
    /**
     * The head that any uh inventory file most have to be imported
     */
    private static final String UH_INVENTORY_FILE_HEAD_CODE = "Archivo UH para importar...";

    public static void main(String... args) {

        //Loading txtFileData file

        String rootPath = "/home/pereiro/MyData/University/Projects/Biología/Programs/InventarioUH/Files/";
        String txtFileName = "activos_fijos_list_centro_costo 2041 pdf 8-6-18.txt";
        File txtFile = new File(rootPath + File.separator + txtFileName);
        ArrayList<String> txtFileData;
        try {
            txtFileData = readTextFile(txtFile);
            if (txtFileData == null) {
                System.out.println("Error leyendo el fichero " + txtFile.getPath());
                return;
            }
        } catch (IOException e) {
            System.out.println("Error leyendo el fichero " + txtFile.getPath());
            e.printStackTrace();
            return;
        }

        //CSV Data
        ArrayList<String> csvData = new ArrayList<>();


        //region extracting data

        boolean mainLineFounded = false;
        boolean firstTime = true;
        String area = "";
        for (String line : txtFileData) {

            //not interested ones
            if (mainLineFounded && (isEndLine(line) || isMCLine(line))) {
                mainLineFounded = false;
            }

            //checking for area line
            if (isAreaLine(line)) {

                area = getArea(line);
                mainLineFounded = false;

                //Info
                System.out.println("Area line found....");
                System.out.println(line);
                System.out.println("\n");


            } else
                //checking for main line
                if (isMainLine(line)) {

                    if (!firstTime) {
                        csvData.add(formatData());
                    }

                    firstTime = false;

                    mainLineFounded = true;

                    getDataFromMainLine(line);
                    TXTtoCSVConverterAppCompatible.area = area;


                    //Info
                    System.out.println("Main line found....");
                    System.out.println(line);
                    System.out.println("\n");

                } else {
                    //checking for descriptionTale line
                    if (mainLineFounded && !line.equals("")) {

                        String tale = getDescriptionTale(line);
                        description += " " + tale;

                        //Info
                        System.out.println("descriptionTale line found....");
                        System.out.println(line);
                        System.out.println("\n");

                    }
                }
        }

        //last row
        csvData.add(formatData());

        //endregion extracting data

        //END
        System.out.println("\n");
        System.out.println("\n");
        System.out.println(csvData.size() + " numbers processed!!!");

        //ToWrite
        System.out.println("Writing File");
        StringBuilder toWrite = new StringBuilder();

        //Head
        toWrite.append(UH_INVENTORY_FILE_HEAD_CODE).append(",").
                append(getDataHashCode(csvData, 0)).append("\n");

        for (String s : csvData) {
            toWrite.append(s);
            toWrite.append("\n");
        }

        //writing
        try {
            String ext = ".csv";
            writeTextFile(new File(rootPath + File.separator +
                    txtFileName.replace(".txt", ext)), toWrite.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("END..............................................");
    }

    //region get data Methods

    private static void getDataFromMainLine(String mainLine) {

        //getting number from a main line
        String number = "";
        for (char c : mainLine.toCharArray()) {
            if (c != ' ') {
                number += c;
            } else {
                break;
            }
        }

        TXTtoCSVConverterAppCompatible.number = number;

        //getting description index
        final int SPACES_GROUP_DEFINITION = 3;
        int counter = 0;
        String spacesInARow = "";
        int dSIndex = 0;
        for (char c : mainLine.toCharArray()) {
            if (c == ' ') {
                spacesInARow += "s";
            } else {
                if (spacesInARow.length() > SPACES_GROUP_DEFINITION) {
                    counter++;
                }
                spacesInARow = "";
            }
            if (counter == 2) {
                break;
            }
            dSIndex++;
        }

        //getting description length
        int dEIndex = dSIndex;
        for (char c : mainLine.substring(dSIndex).toCharArray()) {
            if (c == ' ') {
                spacesInARow += "s";
            } else {
                if (spacesInARow.length() > SPACES_GROUP_DEFINITION) {
                    dEIndex -= spacesInARow.length();
                    break;
                }
                spacesInARow = "";
            }
            dEIndex++;
        }

        //description
        description = mainLine.substring(dSIndex, dEIndex);

        //Alta date
        String subMainLine = mainLine.substring(dEIndex);
        int aDSIndex = subMainLine.indexOf("/") - 2;
        String altaDate = subMainLine.substring(aDSIndex, aDSIndex + 10);
        System.out.println("Alta Date: (" + altaDate + ")");
        TXTtoCSVConverterAppCompatible.altaDate = altaDate;

        //UpdateDate
        subMainLine = subMainLine.substring(aDSIndex + 10);
        int uSIndex = subMainLine.indexOf("/") - 2;
        String updateDate = subMainLine.substring(uSIndex, uSIndex + 10);
        System.out.println("Update Date: (" + updateDate + ")");
        TXTtoCSVConverterAppCompatible.updateDate = updateDate;
    }

    /**
     * return the area if you pass it an area line, the method do not check if the line is an area line,
     *
     * @param areaLine
     * @return
     */
    private static String getArea(String areaLine) {
        if (areaLine.contains(" - ")) {
            return areaLine.substring(areaLine.indexOf(" - ") + 3).
                    replaceAll(" {2,}", "");
        } else {
            System.out.println("Error getting area");
            return areaLine;
        }
    }

    private static String getDescriptionTale(String taleLine) {

        //start index
        int si = 0;
        for (char c : taleLine.toCharArray()) {
            if (c == ' ') {
                si++;
            } else {
                break;
            }
        }

        //end index
        int ei = taleLine.length() - 1;
        for (int i = taleLine.length() - 1; i > 0; i--) {
            if (taleLine.toCharArray()[i] != ' ') {
                ei = i;
                break;
            }
        }


        return taleLine.substring(si, ei + 1);
    }

    /**
     * return a hash code of a given subPart of the ArrayList, it first concatenate all
     * the data as an String, and use the myStringHashCode method to return.
     * This method is important because is the same used by the application to check the hash code
     * of the file. The ArrayList is the data of the file separated in lines using \n as separator.
     *
     * @param data       the data
     * @param startIndex the startIndex to considerate
     * @return the hashcode
     */
    private static int getDataHashCode(ArrayList<String> data, int startIndex) {

        StringBuilder stringBuilder = new StringBuilder();

        for (; startIndex < data.size(); startIndex++) {
            stringBuilder.append(data.get(startIndex));
        }

        return myStringHashCode(stringBuilder.toString());
    }

    //endregion get Data Methods

    //region CSVFile Methods

    /**
     * This method ensure that all the data have any app incompatible characters.
     *
     * @return
     */
    private static String formatData() {
        return checkData(number) + "," + checkData(description) + "," + checkData(area) + "," +
                checkData(altaDate) + "," + checkData(updateDate);
    }

    private static ArrayList<String> readTextFile(File file) throws IOException {
        ArrayList<String> text = new ArrayList<>();
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String temp;
        while ((temp = br.readLine()) != null) {
            text.add(temp);
        }
        return text;
    }

    private static void writeTextFile(File file, String data) throws IOException {
        FileWriter fw = new FileWriter(file);
        fw.write(data);
        fw.close();
    }

    private static int myStringHashCode(String s) {
        int hash = 0;
        for (int i = 0; i < s.length(); i++) {
            hash += s.charAt(i) * 31 ^ (s.length() - i + 1);
        }
        return hash;
    }

    //endregion CSVFile Methods

    //region Checkers...

    private static boolean isAreaLine(String line) {
        return line.contains("Area de Responsabilidad:");
    }

    private static boolean isMainLine(String line) {
        return line.contains("ALINA FORRELLAT BARRIO");
    }

    private static boolean isMCLine(String line) {
        return line.contains("M.C.:");
    }

    private static boolean isEndLine(String line) {
        return line.contains("Elaborado por:") && line.contains("Responsable:") && line.contains("Revisado por:");
    }

    private static String checkData(String data) {

        String checkedData = data.replaceAll(",", ".").
                replaceAll("\"", "").
                replaceAll("\n", "").
                replaceAll("\r", "");

        while (checkedData.contains("  ")) {
            checkedData = checkedData.replaceAll(" {2}", " ");
        }

        if (checkedData.toCharArray()[0] == ' ') {
            checkedData = checkedData.substring(1);
        }

        return checkedData;
    }

    //endregion


}
