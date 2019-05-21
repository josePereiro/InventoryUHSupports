package CSVFile;

import MyJavaTools.FileTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CSVFile {

    public static class InventoryUH {

        /**
         * Read the given file as an InventoryUH csvFile. It handle the reading of any data in the file.
         */
        public static class Reader {

            private ArrayList<String> data;
            private int hashCode;

            public Reader(File file) throws IOException, CorruptedFileException, BrokenDataException {

                data = FileTools.readTextFile(file);
                Tools.checkDataSign(data);
                Tools.checkDataFormat(data);
                hashCode = Tools.getFileHashCode(data);
            }

            /**
             * @return An arrayList containing all the numbers of the file.
             */
            public ArrayList<String> getNumbers() {
                return getColumn(Format.NUMBER_INDEX);
            }

            /**
             * @return An arrayList containing all the descriptions of the file.
             */
            public ArrayList<String> getDescriptions() {
                return getColumn(Format.DESCRIPTION_INDEX);
            }

            /**
             * @return An arrayList containing all the alta dates of the file.
             */
            public ArrayList<String> getAltaDates() {
                return getColumn(Format.ALTA_INDEX);
            }

            /**
             * @return An arrayList containing all the areas of the file.
             */
            public ArrayList<String> getAreas() {
                return getColumn(Format.AREA_INDEX);
            }

            /**
             * @return An arrayList containing all the update dates of the file.
             */
            public ArrayList<String> getUpdateDate(){return getColumn(Format.UPDATE_INDEX);}

            public int getHashCode() {
                return hashCode;
            }

            public int getLineCount() {
                return data.size();
            }

            public ArrayList<String> getDataCopy() {
                ArrayList<String> copy = new ArrayList<>();
                for (String datum : data) {
                    copy.add(datum);
                }
                return copy;
            }

            public String getLine(int lineIndex) {
                return data.get(lineIndex);
            }

            private ArrayList<String> getColumn(int columnIndex) {
                try {
                    ArrayList<String> columnValues = new ArrayList<>();
                    for (int l = Format.FIRST_DATA_LINE_INDEX; l < data.size(); l++) {
                        columnValues.add(data.get(l).split(",")[columnIndex]);
                    }
                    return columnValues;
                } catch (Exception e) {
                    return null;
                }
            }


        }

        /**
         * Check a set of data and if it is correct it signed as an InventoryUH csvFile
         */
        public abstract static class Signer {

            /**
             * Check a set of data and if it is correct it sign it as an InventoryUH csvFile.
             * It will modified the given data instance, so, be careful!!!.
             *
             * @param data the data
             * @throws BrokenDataException if the data has problems.
             */
            public static void signData(ArrayList<String> data) throws BrokenDataException {

                //Checking data format
                Tools.checkDataFormat(data);

                //Hash
                int hashCode = Tools.getDataHashCode(data, 0);

                //Signature Line
                String signLine = Format.UH_INVENTORY_FILE_HEAD_CODE + "," + hashCode;

                //Add Signature Line
                data.add("");
                int last;
                for (int beforeLast = data.size() == 1 ? 0 : data.size() - 2; beforeLast >= 0; beforeLast--) {
                    last = beforeLast + 1;
                    data.set(last, data.get(beforeLast));
                }
                data.set(0, signLine);
            }
        }

        /**
         * Have all the features of the format of the InventoryUH csvFile
         */
        private static class Format {

            static final int META_DATA_LINE_INDEX = 0;
            static final int HASH_CODE_INDEX = 1;
            static final int HEAD_INDEX = 0;
            static final int FIRST_DATA_LINE_INDEX = 1;
            static final int COLUMNS_COUNT = 5;

            static final int NUMBER_INDEX = 0;
            static final int DESCRIPTION_INDEX = 1;
            static final int AREA_INDEX = 2;
            static final int ALTA_INDEX = 3;
            static final int UPDATE_INDEX = 4;

            static final String UH_INVENTORY_FILE_HEAD_CODE = "Archivo UH para importar...";


            /**
             * This method set to the proper format the text to be part of an inventory uh file...
             * As the main store format are csv files, commas are not allowed. It is only allowed uppercase
             * alphanumeric chars, ' ', '/' , '-', '_' and '.'. That is all!!!! Commas are change by points, double spaces are change
             * for single ones and spaces at the beginning or at the en are eliminated.
             * To keep some track of changes, any other no legal char are
             * changed by '_'.
             *
             * @param data the virgin data
             * @return the data with some changes :)
             */
            public static String formatData(String data) {
                return data.replaceAll(",", ".").
                        replaceAll(" {2,}", " ").
                        replaceAll("^ ", "").
                        replaceAll(" $", "").
                        toUpperCase().
                        replaceAll("[^A-Z0-9_\\-\\. /]", "_");
            }

            /**
             * This just change the data a little bet to be compatible with csv files...
             * Changes ',' by '.' and eliminate the extra spaces!!!
             * This format is not save for the inventory UH app
             *
             * @param data
             * @return
             */
            static String readableFormatData(String data) {
                return data.replaceAll(",", ".").
                        replaceAll(" {2,}", " ").
                        replaceAll("^ ", "").
                        replaceAll(" $", "").
                        toUpperCase();
            }

        }

        /**
         *
         */
        public static class TxtToCsvConverter {

            //Deb
            private static final java.util.Scanner scanner = new java.util.Scanner(System.in);
            private static boolean stepping = true;

            //Patterns
            private static final Pattern numberLinePattern =
                    Pattern.compile("(?<number>^[0-9a-zA-Z]+[-]?[0-9a-zA-Z]*)" +
                            "(?<spacer1>[ ]+)" +
                            "(?<number2>[0-9a-zA-Z]+[-]?[0-9a-zA-Z]*)" +
                            "(?<spacer2>[ ]+)" +
                            "(?<description>[[^ ]*[ ]+]*[^ ]+)" +
                            "(?<spacer3>[ ]+)" +
                            "(?<float1>[0-9]+[.][0-9]+)" +
                            "(?<spacer4>[ ]+)" +
                            "(?<float2>[0-9]+[.][0-9]+)" +
                            "(?<spacer5>[ ]+)" +
                            "(?<float3>[0-9]+[.][0-9]+)" +
                            "(?<spacer6>[ ]+)" +
                            "(?<float4>[0-9]+[.][0-9]+)" +
                            "(?<spacer7>[ ]+)" +
                            "(?<responsable>MADAY ALONSO DEL RIV)" +
                            "(?<spacer8>[ ]+)" +
                            "(?<state>[[^ ]*[ ]+]*[^ ]+)" +
                            "(?<spacer9>[ ]+)" +
                            "(?<alta>[0-9]{1,2}/[0-9]{1,2}/[0-9]{1,4})" +
                            "(?<spacer10>[ ]+)" +
                            "(?<update>[0-9]{1,2}/[0-9]{1,2}/[0-9]{1,4})" +
                            "");

            /**
             * ____Elaborado_por:_______________________________________________Responsable:_____________________________________________Revisado_por:_
             */
            private static final Pattern mcLinePattern = Pattern.compile(".*[ ]+[M][.][C][.][:][ ]+.*");
            private static final Pattern bottomLine1Pattern = Pattern.compile("^.+([Elaborado por:]).+(Responsable:).+(Revisado por).*");
            private static final Pattern bottomLine2Pattern = Pattern.compile("^.+([Firma:]).+(Firma:).+(Firma:).*");
            private static final Pattern extraDescriptionPattern = Pattern.compile("^[ ]+(?<extraDescription>[[^ ]*[ ]+]*[^ ])+[ ]*");
            private static Pattern areaLinePattern = Pattern.compile("(Area de Responsabilidad:)([ ]+)([0-9]+-?[0-9]+)( - )(?<area>[[^ ]+[ ]+]*[^ ]+)");


            public static void main(String[] args) {

                //Selecting File
                printInfoAndWait("Welcome to ta TxtToCsv inventory converter_v3.5.\n" +
                        " Selecting a txt file!!!");
                System.out.println("Wait till the dialog appears!!!");
                System.out.println();
                File txtFile = FileTools.openFileDialog("Select a txt inventory txtFile!!!");
                if (txtFile != null) {

                    String[] txtLines;
                    //Loading Data
                    try {
                        txtLines = FileTools.readTextFile(txtFile).toArray(new String[0]);
                    } catch (IOException e) {
                        e.printStackTrace();
                        printInfoAndWait("ERROR loading file " + txtFile.getAbsolutePath());
                        return;
                    }
                    printInfoAndWait("File loaded!!! Line count: " + txtLines.length);
                    System.out.println(" Precesing text!!!");

                    String line;
                    ArrayList<String> csvLines = new ArrayList<>();
                    String number;
                    String description;
                    String area = "";
                    String alta;
                    String update;
                    Matcher lineMatcher;
                    for (int li = 0; li < txtLines.length; li++) {
                        line = txtLines[li];
                        System.out.println();
                        System.out.println(li + ": new txt line");

                        //Classifying line
                        lineMatcher = numberLinePattern.matcher(line);
                        if (lineMatcher.find()) {

                            //Getting data from number line
                            printInfoAndWait(li + ": Number line detected: " + line.replaceAll(" ", "_"));
                            System.out.println(li + ": Getting dates from line");

                            number = lineMatcher.group("number");
                            System.out.println(li + ": Number -> " + number);

                            description = lineMatcher.group("description");
                            System.out.println(li + ": Description -> " + description);

                            alta = lineMatcher.group("alta");
                            System.out.println(li + ": alta -> " + alta);

                            update = lineMatcher.group("update");
                            printInfoAndWait(li + ": update -> " + update);
                            System.out.println(li + " Checking for extra description lines  ");

                            //Checking next till mcLine
                            do {
                                li++;
                                if (li >= txtLines.length) {
                                    break;
                                }

                                line = txtLines[li];
                                System.out.println();
                                System.out.println(li + ": new txt line");

                                if (mcLinePattern.matcher(line).find()) {
                                    System.out.println(li + ": M.C.: line detected: " + line.replaceAll(" ", "_"));
                                    break;
                                }

                                if (bottomLine1Pattern.matcher(line).matches() || bottomLine2Pattern.matcher(line).matches()) {
                                    System.out.println(li + ": bottoms lines detected: " + line.replaceAll(" ", "_"));
                                    break;
                                }

                                if (line.equals("")) {
                                    System.out.println(li + ": Nothing useful detected: " + line.replaceAll(" ", "_"));
                                    continue;
                                }
                                lineMatcher = extraDescriptionPattern.matcher(line);
                                if (lineMatcher.matches()) {
                                    System.out.println(li + " Extra description line detected: " + line.replaceAll(" ", "_"));
                                    String extraDescription = lineMatcher.group("extraDescription");
                                    description += " " + extraDescription + " ";
                                    printInfoAndWait(li + ": new description: " + description);
                                }


                            } while (true);

                            //Adding dates
                            String csvLine = Format.readableFormatData(number) +
                                    "," +
                                    Format.readableFormatData(description) +
                                    "," +
                                    Format.readableFormatData(area) +
                                    "," +
                                    Format.readableFormatData(alta) +
                                    "," +
                                    Format.readableFormatData(update);
                            System.out.println();
                            if (number.equals("") || description.equals("") ||
                                    area.equals("") || alta.equals("") || update.equals("")) {
                                stepping = true;
                                printInfoAndWait(csvLines.size() + ": Something smell bad!!! Check this line: " + csvLine);
                            } else {
                                csvLines.add(csvLine);
                                printInfoAndWait(csvLines.size() + ": New csv line created: " + csvLine);
                            }


                        } else {

                            lineMatcher = areaLinePattern.matcher(line);
                            if (lineMatcher.find()) {
                                System.out.println(li + ": Area line detected: " + line.replaceAll(" ", "_"));
                                area = lineMatcher.group("area");
                                printInfoAndWait(li + ": area: " + area);

                            } else {
                                System.out.println(li + ": Nothing useful detected: " + line.replaceAll(" ", "_"));
                            }
                        }
                    }

                    //Writing file
                    System.out.println();
                    System.out.println("Process ends");
                    System.out.println(csvLines.size() + " number data found...");
                    System.out.println();
                    printInfoAndWait("Writing file!!!");
                    StringBuffer toWrite = new StringBuffer();
                    if (csvLines.size() == 0) {
                        printInfoAndWait("ERROR. You lose the time, for some reason nothing was store. Check the input file!!!");
                        return;
                    }
                    toWrite.append(csvLines.get(0));
                    for (int li = 1; li < csvLines.size(); li++) {
                        toWrite.append("\n");
                        toWrite.append(csvLines.get(li));
                    }
                    String csvFileName = txtFile.getName().substring(0, txtFile.getName().length() - ".txt".length()) + ".csv";
                    File csvFile = new File(txtFile.getParentFile(), csvFileName);
                    try {
                        FileTools.writeTextFile(csvFile, toWrite.toString());
                    } catch (IOException e) {
                        printInfoAndWait("ERROR: Was unable to write the csvFile!!! Sorry.");
                        return;
                    }

                    printInfoAndWait("File created, check it at: " + csvFile.getAbsolutePath());


                } else {
                    System.out.println("None txtFile selected!!!");
                }
            }

            private static void printInfoAndWait(String info) {
                if (stepping) {
                    System.out.println(info);
                    System.out.println("Press enter to continue or 'h' for help!!!");
                    String input = scanner.nextLine().toLowerCase();
                    scanner.reset();
                    if (input.equals("h")) {
                        printInfoAndWait("A small help\nUse this program is really simple, just load a txt file derive from a inventoryUH pdf and " +
                                "it tries to split it to form a csv file. It will create a csv file with the same name as the input " +
                                "and in the same location. The produced csvFile for this program is not ready yet to be imported " +
                                "to any applications as inventarioUH.\n" +
                                "Warning: It will overlap any file with the same name and in the same path!!!" +
                                "FoxitReader is the most recommended program to convert pdf to txt!!!\n" +
                                "If you convert and load a file that you know is trustworthy, and anyway the convection " +
                                "fails, communicate with apereiro@fbio.uh.cu for some help :)\n" +
                                "Program flux:\n" +
                                "The program has two modes, stepping and running. While stepping the program stop after any action" +
                                ", show information and wait till you press enter to continue. In running mode it shows " +
                                "info but will no wait for you. In fact you can change the modes only once. Right now the program is in stepping" +
                                " mode, it is waiting, so you can read this. To change the mode type 'r' and press enter. But remember, after that you " +
                                "have no more controls over the program flux");
                    } else if (input.equals("r")) {
                        stepping = false;
                        printInfoAndWait("------------------------------------- !!!! Run mode activated !!!!");
                    }

                } else {
                    System.out.println(info);
                }
            }

        }
    }

    private static class Tools {

        /**
         * Produce the custom hash code. I use my own for cross platform compatibility.
         *
         * @param s
         * @return
         */
        private static int myStringHashCode(String s) {
            int hash = 0;
            for (int i = 0; i < s.length(); i++) {
                hash += s.charAt(i) * 31 ^ (s.length() - i + 1);
            }
            return hash;
        }

        /**
         * Check if the data is correctly signed!!! It will return true for
         * InventoryUH or BackUp csv files with valid signs.
         *
         * @param csvFileData the lines of the csv file
         * @return true if everything if ok
         */
        private static void checkDataSign(ArrayList<String> csvFileData) throws CorruptedFileException {
            try {
                if (!(getDataHashCode(csvFileData, InventoryUH.Format.FIRST_DATA_LINE_INDEX) == getFileHashCode(csvFileData) &&
                        getFileHead(csvFileData).equals(InventoryUH.Format.UH_INVENTORY_FILE_HEAD_CODE)))
                    throw new CorruptedFileException("The file has not a right " +
                            "format or is corrupted!!!");
            } catch (Exception e) {
                throw new CorruptedFileException("The file has not a right " +
                        "format or is corrupted!!!");
            }
        }

        /**
         * Check if the data in the csvFile is in the proper format
         *
         * @param csvFileData
         * @return
         */
        private static void checkDataFormat(ArrayList<String> csvFileData) throws BrokenDataException {

            for (int line = InventoryUH.Format.FIRST_DATA_LINE_INDEX; line < csvFileData.size(); line++) {
                if (csvFileData.get(line).split(",", -1).length != InventoryUH.Format.COLUMNS_COUNT) {
                    throw new BrokenDataException("The data is broken in line: " + line +
                            " No proper data count in this line!!!. Proper data count " +
                            InventoryUH.Format.COLUMNS_COUNT);
                }
            }
        }

        /**
         * Return a hash code of a given subPart of the ArrayList, it first concatenate all
         * the data as an String, and use the myStringHashCode method to return.
         * This method is important because is the same used by the application to check the hash code
         * of the file. The ArrayList is the data of the file separated in lines using \n as separator.
         *
         * @param data the csvData data
         * @return the hashcode calculated from the data
         */
        private static int getDataHashCode(ArrayList<String> data, int dataStartIndex) {

            StringBuilder stringBuilder = new StringBuilder();

            for (int line = dataStartIndex; line < data.size(); line++) {
                stringBuilder.append(data.get(line));
            }

            return myStringHashCode(stringBuilder.toString());
        }

        /**
         * Return the hash code stored in the file data.
         *
         * @param data the csvFile data
         * @return the hashcode stored in the data
         */
        private static int getFileHashCode(ArrayList<String> data) {
            return Integer.parseInt(data.get(InventoryUH.Format.META_DATA_LINE_INDEX).split(",")[InventoryUH.Format.HASH_CODE_INDEX]);
        }

        /**
         * Return the head stored in the file.
         *
         * @param data the csvFile data
         * @return
         * @throws Exception
         */
        private static String getFileHead(ArrayList<String> data) throws Exception {
            return data.get(InventoryUH.Format.META_DATA_LINE_INDEX).split(",")[InventoryUH.Format.HEAD_INDEX];
        }

    }

    public static class CorruptedFileException extends Exception {

        CorruptedFileException(String ms) {
            super(ms);
        }
    }

    public static class BrokenDataException extends Exception {

        BrokenDataException(String ms) {
            super(ms);
        }
    }


}
