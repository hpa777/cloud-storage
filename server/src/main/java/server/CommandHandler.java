package server;

import commands.Command;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandHandler {

    private final static Logger logger = Logger.getLogger(CommandHandler.class.getName());

    private static final String ROOT_NOTIFY = "You are already in the root directory\n\r";
    private static final String DIRECTORY_DOESNT_EXIST = "Directory or file %s doesn't exist\n\r";

    /**
     * Корневая директория
     */
    private Path rootPath;

    /**
     * Текущая директория
     */
    private Path currentPath;

    private DbHelper dbHelper;

    public CommandHandler(Connection connection) {
        this.dbHelper = new DbHelper(connection);
    }

    /**
     * Обработчик полученных команд
     *
     * @param msg
     * @return
     * @throws IOException
     */
    public Object doCommand(Object msg) throws IOException {
        // Если ожидалась загрузка файла, получаем файл и сохраняем его.
        if (uploadFile != null) {
            return saveFile(msg);
        }
        String command = msg.toString().replace("\n", "").replace("\r", "");
        logger.info("Receive command: " + command);
        // Если пользователь не аутенифицирован, переходим к атентификации или регистрации.
        if (rootPath == null) {
            return authenticate(command);
        }
        Object result = null;
        if (Command.LIST_FILES.equals(command)) {
            result = listFiles();
        } else if (Command.GET_CURRENT_PATH.equals(command)) {
            result = getCurrentPath();
        } else if (Command.PASTE.equals(command)) {
            result = paste();
        } else if (command.startsWith(Command.DOWNLOAD)) {
            result = download(command);
        } else if (command.startsWith(Command.UPLOAD)) {
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
        } else if (command.startsWith(Command.FIND)) {
            result = find(command);
        }
        return result;
    }

    /**
     * Аутентификаци/Регистрация
     * В случае успеха устанавливается корневая директория пользователя.
     *
     * @param command
     * @return
     */
    private Object authenticate(String command) {
        String[] d = command.split(Command.DELIMITER);
        Long uid = null;
        if (command.startsWith(Command.LOGIN)) {
            uid = dbHelper.getUserId(d[1], d[2]);
        } else if (command.startsWith(Command.REGISTRATION)) {
            uid = dbHelper.registration(d[1], d[2], d[3]);
        }
        if (uid != null) {
            rootPath = getRootPath(uid);
            currentPath = rootPath;
            if (rootPath != null) return Command.OK;
        }
        return Command.FAIL;
    }

    /**
     * Возвращает корневую директорию пользователя (root_server_dir/user_id). Если не существует - создаем
     *
     * @param uid Long
     * @return Path
     */
    private Path getRootPath(Long uid) {
        Path path = Path.of(Settings.getInstance().getRootPath(), Long.toString(uid));
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                path = null;
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return path;
    }

    /**
     * Возвращает "Breadcrumbs"
     * @return
     */
    private Object getCurrentPath() {
        return String.format("%s %s"
                , dbHelper.getUserName()
                , currentPath.toString().replace(rootPath.toString(), "~"));
    }

    private boolean found;

    /**
     * Поиск файла/директории
     * @param command
     * @return
     */
    private Object find(String command) {
        String file = command.split(" ")[1].toLowerCase(Locale.ROOT);
        found = false;
        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
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
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return found ? Command.OK : Command.FAIL;
    }

    /**
     * Переименование файла/директории
     * @param command
     * @return
     */
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
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return Command.OK;
    }



    private Path uploadFile;

    /**
     * Готвимся получить файл.
     * @param command
     * @return
     */
    private Object upload(String command) {
        String dir = command.split(" ")[1];
        uploadFile = Path.of(currentPath.toString(), dir);
        if (Files.exists(uploadFile)) {
            uploadFile = null;
            return Command.ALREADY_EXISTS;
        }
        return Command.WAIT;
    }

    /**
     * Сохраняем полученный файл
     * @param msg
     * @return
     */
    private Object saveFile(Object msg) {
        try {
            Files.write(uploadFile, (byte[]) msg);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            uploadFile = null;
        }
        return Command.OK;
    }

    /**
     * Отправляем файл
     * @param command
     * @return
     * @throws IOException
     */
    private Object download(String command) throws IOException {
        String dir = command.split(" ")[1];
        Path neededPath = Path.of(currentPath.toString(), dir);
        return Files.readAllBytes(neededPath);
    }

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    /**
     * Отправляем список файлов
     * @return
     * @throws IOException
     */
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

    /**
     * Возвращает размер директории
     * @param folder
     * @return
     * @throws IOException
     */
    public long getFolderSize(Path folder) throws IOException {
        return Files.walk(folder)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();
    }

    /**
     * Подготовка к копированию. Запоминаем источник
     * @param command
     * @return
     */
    private Object copy(String command) {
        String[] arg = command.split(" ");
        sourcePathForCopy = Path.of(currentPath.toString(), arg[1]);
        return Command.OK;
    }

    /**
     * Путь источника копирования
     */
    private Path sourcePathForCopy;

    /**
     * Копирование. Если копируется директория - рекурсивно копируем содержимое.
     * @return
     * @throws IOException
     */
    private Object paste() throws IOException {
        if (!Files.exists(sourcePathForCopy)) {
            return null;
        }
        if (Files.isDirectory(sourcePathForCopy)) {
            Files.walkFileTree(sourcePathForCopy, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path path = currentPath.resolve(sourcePathForCopy.relativize(dir));
                    Files.createDirectories(path);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path target = currentPath.resolve(sourcePathForCopy.relativize(file));
                    Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

            });
        } else {
            Path target = currentPath.resolve(sourcePathForCopy.getFileName());
            Files.copy(sourcePathForCopy, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return Command.OK;
    }

    /**
     * Удаление файла и директории (рекурсивно)
     * @param command
     * @return
     * @throws IOException
     */
    private Object remove(String command) throws IOException {
        String dir = command.split(" ")[1];
        Path neededPath = Path.of(currentPath.toString(), dir);
        Files.walk(neededPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        return Command.OK;
    }

    /**
     * Создание директории
     * @param command
     * @return
     * @throws IOException
     */
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

    /**
     * Смена текущей директории
      * @param command
     * @return
     */
    private String replacePosition(String command) {
        String ret = "";
        String neededPath = command.split(" ")[1];
        Path tempPath = Path.of(currentPath.toString(), neededPath);
        if (Command.GO_UP_DIR.equals(neededPath)) {
            tempPath = currentPath.getParent();
            if (tempPath == null || !tempPath.toString().startsWith(rootPath.toString())) {
                ret = ROOT_NOTIFY;
            } else {
                currentPath = tempPath;
            }
        } else if (Command.GO_ROOT_DIR.equals(neededPath)) {
            currentPath = rootPath;
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
