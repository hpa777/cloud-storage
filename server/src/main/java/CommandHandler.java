import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandHandler {

    private static final String ROOT_NOTIFY = "You are already in the root directory\n\r";
    private static final String DIRECTORY_DOESNT_EXIST = "Directory or file %s doesn't exist\n\r";
    private static final String DIRECTORY_ALREADY_EXIST = "Director or file %s already exist\n\r";
    private static final String NO_SUCH_FILE = "%s: no such file or directory\n\r";
    private static final String DIR_NOT_EMPTY = "%s not empty\n\r";

    private Path currentPath;

    private String nickName;

    public CommandHandler(String nickName) {
        this.currentPath = Path.of(Server.ROOT_PATH);
        this.nickName = nickName;
    }

    public Object doCommand(String command) throws IOException {
        command = command.replace("\n", "").replace("\r", "");
        StringBuilder response = new StringBuilder();
        if ("ls".equals(command)) {
            //response.append(getFileList().concat("\n\r"));
            return listFilesUsingFileWalkAndVisitor(currentPath.toString());
        } else if (command.startsWith("nick ")) {
            changeNickname(command);
        } else if (command.startsWith("cd ")) {
            response.append(replacePosition(command));
        } else if (command.startsWith("mkdir ")) {
            response.append(createDirectory(command));
        } else if (command.startsWith("rm ")) {
            response.append(remove(command));
        } else if (command.startsWith("copy ")) {
            copy(command);
        } else if (command.startsWith("cat ")) {
            response.append(showFile(command));
        } else if (command.startsWith("touch ")) {
            response.append(createFile(command));
        }
        response.append(getHello());
        return response.toString();
    }

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public Set<String> listFilesUsingFileWalkAndVisitor(String dir) throws IOException {
        Set<String> fileList = new HashSet<>();
        Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                fileList.add(String.join(";"
                        , file.getFileName().toString()
                        , attrs.isDirectory() ? "dir" : ""
                        , Long.toString(attrs.size())
                        , simpleDateFormat.format(attrs.creationTime().toMillis())
                        , simpleDateFormat.format(attrs.lastModifiedTime().toMillis())
                ));
                return FileVisitResult.CONTINUE;
            }
        });
        return fileList;
    }

    private String createFile(String command) throws IOException {
        String ret = "";
        String dir = command.split(" ")[1];
        Path neededPath = Path.of(currentPath.toString(), dir);
        if (Files.exists(neededPath) && !Files.isDirectory(neededPath)) {
            ret = String.format(DIRECTORY_ALREADY_EXIST, dir);
        } else {
            Files.createFile(neededPath);
        }
        return ret;
    }

    private String showFile(String command) throws IOException {
        String ret;
        String dir = command.split(" ")[1];
        Path neededPath = Path.of(currentPath.toString(), dir);
        if (Files.exists(neededPath) && !Files.isDirectory(neededPath)) {
            StringBuilder sb = new StringBuilder();
            Files.readAllLines(neededPath).forEach(sb::append);
            ret = sb.toString();
        } else {
            ret = String.format(DIRECTORY_DOESNT_EXIST, dir);
        }
        return ret;
    }

    private void copy(String command) throws IOException {
        String[] arg = command.split(" ");
        if (arg.length != 3) return;
        Path sourcePath = Path.of(currentPath.toString(), arg[1]);
        Files.walkFileTree(sourcePath, new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String fileName = dir.toString().substring(sourcePath.toString().length());
                Path targetPath = Path.of(currentPath.toString(), arg[2], fileName);
                Files.createDirectory(targetPath);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.toString().substring(sourcePath.toString().length());
                Path targetPath = Path.of(currentPath.toString(), arg[2], fileName);
                Files.copy(file, targetPath);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private String remove(String command) throws IOException {
        String ret = "";
        String dir = command.split(" ")[1];
        Path neededPath = Path.of(currentPath.toString(), dir);
        try {
            //BasicFileAttributes attr = Files.readAttributes(neededPath, BasicFileAttributes.class);
            //FileTime t = attr.creationTime();

            Files.delete(neededPath);
        } catch (NoSuchFileException e) {
            ret = String.format(NO_SUCH_FILE, dir);
        } catch (DirectoryNotEmptyException e) {
            ret = String.format(DIR_NOT_EMPTY, dir);
        }
        return ret;
    }

    private String createDirectory(String command) throws IOException {
        String ret = "";
        String dir = command.split(" ")[1];
        Path neededPath = Path.of(currentPath.toString(), dir);
        if (Files.exists(neededPath)) {
            ret = String.format(DIRECTORY_ALREADY_EXIST, dir);
        } else {
            Files.createDirectory(neededPath);
        }
        return ret;
    }

    private String getHello() {
        String currentPathString = this.currentPath.toString().replace(Server.ROOT_PATH, "~");
        return String.format("%s>:%s$", this.nickName, currentPathString);
    }

    private void changeNickname(String command) {
        nickName = command.split(" ")[1];
    }

    private String getFileList() {
        return String.join(" ", new File(currentPath.toString()).list());
    }

    private String replacePosition(String command) {
        String ret = "";
        String neededPath = command.split(" ")[1];
        Path tempPath = Path.of(currentPath.toString(), neededPath);
        if ("..".equals(neededPath)) {
            tempPath = currentPath.getParent();
            if (tempPath == null || !tempPath.toString().startsWith(Server.ROOT_PATH)) {
                ret = ROOT_NOTIFY;
            } else {
                currentPath = tempPath;
            }
        } else if ("~".equals(neededPath)) {
            currentPath = Path.of(Server.ROOT_PATH);
        } else {
            if (tempPath.toFile().exists()) {
                currentPath = tempPath;
            } else {
                ret = String.format(DIRECTORY_DOESNT_EXIST, neededPath);
            }
        }
        return ret;
    }

}
