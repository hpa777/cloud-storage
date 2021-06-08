package server;

import commands.Command;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public class CommandHandler {

    private final static Logger logger = Logger.getLogger(CommandHandler.class.getName());

    private static final String ROOT_NOTIFY = "You are already in the root directory\n\r";
    private static final String DIRECTORY_DOESNT_EXIST = "Directory or file %s doesn't exist\n\r";

    private Path currentPath;

    private Path sourcePath;

    private final int rootPathLength;

    public CommandHandler(String nickName) {
        this.currentPath = Path.of(Server.ROOT_PATH);
        this.rootPathLength = currentPath.toString().length();
    }

    public Object doCommand(Object msg) throws IOException {
        if (uploadFile != null) {
            return saveFile(msg);
        }
        String command = msg.toString().replace("\n", "").replace("\r", "");
        logger.info("Receive command: " + command);
        Object result = null;
        if (Command.LIST_FILES.equals(command)) {
            result = listFiles();
        } if (command.startsWith(Command.DOWNLOAD)) {
            result = download(command);
        } if (command.startsWith(Command.UPLOAD)) {
            result = upload(command);
        } else if (command.startsWith(Command.CHANGE_DIR)) {
            result = replacePosition(command);
        } else if (command.startsWith(Command.MAKE_DIR)) {
            result = createDirectory(command);
        } else if (command.startsWith(Command.RENAME)) {
            result = rename(command);
        } else if (command.startsWith(Command.REMOVE)) {
            result = remove(command);
        } else if (command.startsWith(Command.COPY)) {
            result = copy(command);
        } else if (command.equals(Command.PASTE)) {
            result = paste();
        } else if (command.startsWith(Command.FIND)) {
            result = find(command);
        } else if (command.equals(Command.GET_CURRENT_PATH)) {
            result = getCurrentPath();
        }
        return result;
    }

    private Object getCurrentPath() {
        return currentPath.toString().replace(Server.ROOT_PATH, "~");
    }

    private boolean found;

    private Object find(String command) {
        String file = command.split(" ")[1].toLowerCase(Locale.ROOT);
        found = false;
        try {
            Files.walkFileTree(Path.of(Server.ROOT_PATH), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.getFileName().toString().toLowerCase(Locale.ROOT).contains(file)) {
                        currentPath = dir.getParent();
                        found = true;
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path f, BasicFileAttributes attrs) throws IOException {
                    if (f.getFileName().toString().toLowerCase(Locale.ROOT).contains(file)) {
                        currentPath = f.getParent();
                        found = true;
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return found ? Command.OK : Command.FAIL;
    }

    private Object rename(String command) {
        String dir = command.split(" ")[1];
        Path fileToMovePath = Path.of(currentPath.toString(), dir);
        dir = command.split(" ")[2];
        Path targetPath = Path.of(currentPath.toString(), dir);
        if (Files.exists(targetPath)) {
            return Command.ALREADY_EXISTS;
        }
        try {
            Files.move(fileToMovePath, targetPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Command.OK;
    }

    private Object saveFile(Object msg) {
        try {
            Files.write(uploadFile, (byte[])msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            uploadFile = null;
        }
        return Command.OK;
    }

    private Path uploadFile;

    private Object upload(String command) {
        String dir = command.split(" ")[1];
        uploadFile = Path.of(currentPath.toString(), dir);
        if (Files.exists(uploadFile)) {
            uploadFile = null;
            return Command.ALREADY_EXISTS;
        }
        return Command.WAIT;
    }

    private Object download(String command) throws IOException {
        String dir = command.split(" ")[1];
        Path neededPath = Path.of(currentPath.toString(), dir);
        return Files.readAllBytes(neededPath);
    }

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public Set<String> listFiles() throws IOException {
        Set<String> fileList = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath)) {
            for (Path path : stream) {
                    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                    fileList.add(String.join(Command.DELIMITER
                            , path.getFileName().toString()
                            , attrs.isDirectory() ? "dir" : ""
                            , Long.toString(!attrs.isDirectory() ? attrs.size() : getFolderSize(path))
                            , simpleDateFormat.format(attrs.creationTime().toMillis())
                            , simpleDateFormat.format(attrs.lastModifiedTime().toMillis())
                    ));

            }
        }
        return fileList;
    }

    public long getFolderSize(Path folder) throws IOException {
        return Files.walk(folder)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();
    }

    private Object copy(String command) {
        String[] arg = command.split(" ");
        sourcePath = Path.of(currentPath.toString(), arg[1]);
        return Command.OK;
    }

    private Object paste() throws IOException {
        if (!Files.exists(sourcePath)) {
            return null;
        }
        if (Files.isDirectory(sourcePath)) {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path path = currentPath.resolve(sourcePath.relativize(dir));
                    Files.createDirectories(path);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path target = currentPath.resolve(sourcePath.relativize(file));
                    Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

            });
        } else {
            Path target = currentPath.resolve(sourcePath.getFileName());
            Files.copy(sourcePath, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return Command.OK;
    }

    private Object remove(String command) throws IOException {
        String dir = command.split(" ")[1];
        Path neededPath = Path.of(currentPath.toString(), dir);
        Files.walk(neededPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        return Command.OK;
    }

    private Object createDirectory(String command) throws IOException {
        String ret = Command.OK;
        String dir = command.split(" ")[1];
        Path neededPath = Path.of(currentPath.toString(), dir);
        if (Files.exists(neededPath)) {
            ret = Command.ALREADY_EXISTS;
        } else {
            Files.createDirectory(neededPath);
        }
        return ret;
    }

    private String replacePosition(String command) {
        String ret = "";
        String neededPath = command.split(" ")[1];
        Path tempPath = Path.of(currentPath.toString(), neededPath);
        if (Command.GO_UP_DIR.equals(neededPath)) {
            tempPath = currentPath.getParent();
            if (tempPath == null || !tempPath.toString().startsWith(Server.ROOT_PATH)) {
                ret = ROOT_NOTIFY;
            } else {
                currentPath = tempPath;
            }
        } else if (Command.GO_ROOT_DIR.equals(neededPath)) {
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
