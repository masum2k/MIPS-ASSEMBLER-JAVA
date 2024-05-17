import java.io.*;
import java.util.*;

public class MIPSPROJECT1 {

    public static List<String> readSourceFile(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line.trim());
        }
        reader.close();
        return lines;
    }

    public static Map<String, Integer> parseLabels(List<String> lines) {
        Map<String, Integer> labels = new HashMap<>();
        int address = 0x00400000;
        for (String line : lines) {
            if (line.endsWith(":")) {
                labels.put(line.substring(0, line.length() - 1), address);
            } else if (!line.startsWith(".") && !line.isEmpty()) {
                address += 4;
            }
        }
        return labels;
    }

    public static String translateInstruction(String instruction, Map<String, Integer> labels, int currentAddress) {
        String[] parts = instruction.split("[ ,\t]+");
        String opcode = parts[0];

        if (opcode.equals("add")) {
            return String.format("0x%08x", 0x00000020 | (register(parts[1]) << 11) | (register(parts[2]) << 21) | (register(parts[3]) << 16));
        } else if (opcode.equals("sub")) {
            return String.format("0x%08x", 0x00000022 | (register(parts[1]) << 11) | (register(parts[2]) << 21) | (register(parts[3]) << 16));
        } else if (opcode.equals("and")) {
            return String.format("0x%08x", 0x00000024 | (register(parts[1]) << 11) | (register(parts[2]) << 21) | (register(parts[3]) << 16));
        } else if (opcode.equals("or")) {
            return String.format("0x%08x", 0x00000025 | (register(parts[1]) << 11) | (register(parts[2]) << 21) | (register(parts[3]) << 16));
        } else if (opcode.equals("sll")) {
            return String.format("0x%08x", 0x00000000 | (register(parts[1]) << 11) | (register(parts[2]) << 16) | (immediate(parts[3]) << 6));
        } else if (opcode.equals("srl")) {
            return String.format("0x%08x", 0x00000002 | (register(parts[1]) << 11) | (register(parts[2]) << 16) | (immediate(parts[3]) << 6));
        } else if (opcode.equals("sllv")) {
            return String.format("0x%08x", 0x00000004 | (register(parts[3]) << 21) | (register(parts[2]) << 16) | (register(parts[1]) << 11) | 0x00);
        } else if (opcode.equals("srlv")) {
            return String.format("0x%08x", 0x00000006 | (register(parts[3]) << 21) | (register(parts[2]) << 16) | (register(parts[1]) << 11) | 0x00);
        } else if (opcode.equals("addi")) {
            return String.format("0x%08x", 0x20000000 | (register(parts[1]) << 16) | (register(parts[2]) << 21) | (immediate(parts[3]) & 0xFFFF));
        } else if (opcode.equals("andi")) {
            return String.format("0x%08x", 0x30000000 | (register(parts[1]) << 16) | (register(parts[2]) << 21) | (immediate(parts[3]) & 0xFFFF));
        } else if (opcode.equals("lw")) {

            if (parts[2].contains("(") && parts[2].contains(")")) {

                String[] offsetAndBase = parts[2].split("[()]");
                String offset = offsetAndBase[0];
                String base = offsetAndBase[1].replace("$", "");

                return String.format("0x%08x", 0x8C000000 | (register(parts[1]) << 16) | (register(base) << 21) | (Integer.parseInt(offset) & 0xFFFF));
            } else {

                return String.format("0x%08x", 0x8C000000 | (register(parts[1]) << 16) | (register(parts[3]) << 21) | (immediate(parts[2]) & 0xFFFF));
            }
        } else if (opcode.equals("sw")) {

            if (parts[2].contains("(") && parts[2].contains(")")) {

                String[] offsetAndBase = parts[2].split("[()]");
                String offset = offsetAndBase[0];
                String base = offsetAndBase[1].replace("$", "");

                return String.format("0x%08x", 0xAC000000 | (register(parts[1]) << 16) | (register(base) << 21) | (Integer.parseInt(offset) & 0xFFFF));
            } else {

                return String.format("0x%08x", 0xAC000000 | (register(parts[1]) << 16) | (register(parts[3]) << 21) | (immediate(parts[2]) & 0xFFFF));
            }
        } else if (opcode.equals("beq")) {
            return String.format("0x%08x", 0x10000000 | (register(parts[1]) << 21) | (register(parts[2]) << 16) | ((labels.get(parts[3]) - currentAddress - 4) >> 2 & 0xFFFF));
        } else if (opcode.equals("bne")) {
            return String.format("0x%08x", 0x14000000 | (register(parts[1]) << 21) | (register(parts[2]) << 16) | ((labels.get(parts[3]) - currentAddress - 4) >> 2 & 0xFFFF));
        } else if (opcode.equals("blez")) {
            return String.format("0x%08x", 0x18000000 | (register(parts[1]) << 21) | ((labels.get(parts[2]) - currentAddress - 4) >> 2 & 0xFFFF));
        } else if (opcode.equals("bgtz")) {
            return String.format("0x%08x", 0x1C000000 | (register(parts[1]) << 21) | ((labels.get(parts[2]) - currentAddress - 4) >> 2 & 0xFFFF));
        } else if (opcode.equals("j")) {
            return String.format("0x%08x", 0x08000000 | ((labels.get(parts[1]) >> 2) & 0x3FFFFFF));
        } else {
            throw new IllegalArgumentException("Unsupported instruction: " + instruction);
        }
    }


    private static int register(String reg) {
        return Integer.parseInt(reg.replace("$", ""));
    }

    private static int immediate(String imm) {
        return Integer.parseInt(imm);
    }

    public static void writeOutputFile(String filename, List<String> machineCode) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename.replace(".asm", ".obj")));
        int address = 0x00400000;


        writer.write("Address\t\tCode\n");

        for (String code : machineCode) {
            writer.write(String.format("0x%08x\t%s\n", address, code));
            address += 4;
        }
        writer.close();
    }


    public static void main(String[] args) throws IOException {
        String inputFilename = "mycode.asm";
        List<String> lines = readSourceFile(inputFilename);


        lines.removeIf(line -> line.startsWith("#") || line.isEmpty());

        Map<String, Integer> labels = parseLabels(lines);
        List<String> machineCode = new ArrayList<>();

        int address = 0x00400000;
        for (String line : lines) {
            if (!line.endsWith(":") && !line.startsWith(".")) {
                String code = translateInstruction(line, labels, address);
                machineCode.add(code);
                address += 4;
            }
        }

        writeOutputFile(inputFilename, machineCode);
    }
}
