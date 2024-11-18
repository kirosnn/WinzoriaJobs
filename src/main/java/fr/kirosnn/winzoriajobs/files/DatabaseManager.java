package fr.kirosnn.winzoriajobs.files;

import fr.kirosnn.winzoriajobs.events.PlayerXPChangeEvent;
import fr.kirosnn.winzoriajobs.utils.JobBonus;
import fr.kirosnn.winzoriajobs.utils.LevelCalculator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupSQLiteDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dataFolder = new File(plugin.getDataFolder(), "data");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File databaseFile = new File(dataFolder, "jobsdb.db");
            if (!databaseFile.exists()) {
                databaseFile.createNewFile();
            }

            String jdbcUrl = "jdbc:sqlite:" + databaseFile.getPath();
            connection = DriverManager.getConnection(jdbcUrl);
            if (isDebugMode()) {
                plugin.getLogger().info("Connexion SQLite établie avec succès.");
            }

            createTables();
        } catch (ClassNotFoundException e) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Le driver SQLite JDBC est introuvable : " + e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException | SQLException e) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Erreur lors de la configuration de la base de données SQLite : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void createTables() {
        if (connection == null) {
            if (isDebugMode()) {
                plugin.getLogger().severe("La connexion à la base de données n'est pas établie.");
            }
            return;
        }

        String createTableSQL = "CREATE TABLE IF NOT EXISTS player_jobs (" +
                "player_name TEXT PRIMARY KEY," +
                "hunter_xp INTEGER DEFAULT 0," +
                "farmer_xp INTEGER DEFAULT 0," +
                "farmer_level INTEGER DEFAULT 0," +
                "hunter_level INTEGER DEFAULT 0," +
                "hunter_tier INTEGER DEFAULT 0," +
                "farmer_tier INTEGER DEFAULT 0," +
                "harvester_lvl INTEGER DEFAULT 1" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            if (isDebugMode()) {
                plugin.getLogger().info("Table player_jobs vérifiée/créée avec succès.");
            }
        } catch (SQLException e) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Erreur lors de la création de la table : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void addXP(String playerName, String jobType, int xpToAdd) {
        if (connection == null) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Connexion à la base de données non disponible.");
            }
            return;
        }

        if (!jobType.equals("farmer_xp") && !jobType.equals("hunter_xp")) {
            if (isDebugMode()) {
                plugin.getLogger().warning("Type de job invalide : " + jobType);
            }
            return;
        }

        String job = jobType.replace("_xp", "");
        int oldLevel = getPlayerLevel(playerName, job);
        int oldTier = getPlayerTier(playerName, job);
        double finalXP = JobBonus.applyBonus(xpToAdd, oldLevel);

        String updateXPQuery = "UPDATE player_jobs SET " + jobType + " = " + jobType + " + ? WHERE player_name = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(updateXPQuery)) {
            stmt.setInt(1, (int) finalXP);
            stmt.setString(2, playerName);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                updateLevelAndTier(playerName, job);

                int newLevel = getPlayerLevel(playerName, job);
                int newTier = getPlayerTier(playerName, job);

                if (newLevel > oldLevel || newTier > oldTier) {
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null) {
                        PlayerXPChangeEvent event = new PlayerXPChangeEvent(player, job, oldLevel, newLevel, oldTier, newTier);
                        Bukkit.getPluginManager().callEvent(event);
                    }
                }

                if (isDebugMode()) {
                    plugin.getLogger().info("XP ajouté avec succès pour " + playerName + " (" + jobType + ": +" + (int) finalXP + " XP).");
                }
            } else {
                if (isDebugMode()) {
                    plugin.getLogger().info("Le joueur " + playerName + " n'existe pas dans la base de données. Ajout en cours...");
                }
                addPlayerIfNotExists(playerName);
                addXP(playerName, jobType, xpToAdd);
            }
        } catch (SQLException e) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Erreur lors de l'ajout de l'XP pour le joueur " + playerName + " : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public void addPlayerIfNotExists(String playerName) {
        if (connection == null) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Connexion à la base de données non disponible.");
            }
            return;
        }

        String checkPlayerSQL = "SELECT player_name FROM player_jobs WHERE player_name = ?;";
        String insertPlayerSQL = "INSERT INTO player_jobs (player_name) VALUES (?);";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkPlayerSQL)) {
            checkStmt.setString(1, playerName);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                try (PreparedStatement insertStmt = connection.prepareStatement(insertPlayerSQL)) {
                    insertStmt.setString(1, playerName);
                    insertStmt.executeUpdate();
                    if (isDebugMode()) {
                        plugin.getLogger().info("Le joueur " + playerName + " a été ajouté à la base de données.");
                    }
                }
            }
        } catch (SQLException e) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Erreur lors de l'ajout du joueur à la base de données : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                if (isDebugMode()) {
                    plugin.getLogger().info("Connexion à la base de données fermée.");
                }
            }
        } catch (SQLException e) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Erreur lors de la fermeture de la connexion à la base de données : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void updateLevelAndTier(String playerName, String jobType) {
        if (connection == null) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Connexion à la base de données non disponible.");
            }
            return;
        }

        if (!jobType.equals("farmer") && !jobType.equals("hunter")) {
            if (isDebugMode()) {
                plugin.getLogger().warning("Type de job invalide : " + jobType);
            }
            return;
        }

        String getXPQuery = "SELECT " + jobType + "_xp FROM player_jobs WHERE player_name = ?;";
        String updateLevelAndTierQuery = "UPDATE player_jobs SET " + jobType + "_tier = ?, " + jobType + "_level = ? WHERE player_name = ?;";

        try (PreparedStatement xpStmt = connection.prepareStatement(getXPQuery)) {
            xpStmt.setString(1, playerName);
            ResultSet rs = xpStmt.executeQuery();

            if (rs.next()) {
                int totalXP = rs.getInt(jobType + "_xp");
                int newLevel = LevelCalculator.calculateLevel(totalXP);
                int newTier = LevelCalculator.getTier(newLevel);

                try (PreparedStatement updateStmt = connection.prepareStatement(updateLevelAndTierQuery)) {
                    updateStmt.setInt(1, newTier);
                    updateStmt.setInt(2, newLevel);
                    updateStmt.setString(3, playerName);
                    updateStmt.executeUpdate();
                    if (isDebugMode()) {
                        plugin.getLogger().info("Niveau et palier mis à jour pour " + playerName + ": niveau " + newLevel + ", palier " + newTier);
                    }
                }
            }
        } catch (SQLException e) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Erreur lors de la mise à jour du niveau et du palier du joueur " + playerName + " : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public int getPlayerLevel(String playerName, String jobType) {
        if (connection == null) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Connexion à la base de données non disponible.");
            }
            return 0;
        }

        if (!jobType.equals("farmer") && !jobType.equals("hunter")) {
            if (isDebugMode()) {
                plugin.getLogger().warning("Type de job invalide : " + jobType);
            }
            return 0;
        }

        String getLevelQuery = "SELECT " + jobType + "_level FROM player_jobs WHERE player_name = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(getLevelQuery)) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(jobType + "_level");
            } else {
                if (isDebugMode()) {
                    plugin.getLogger().info("Le joueur " + playerName + " n'a pas été trouvé dans la base de données.");
                }
            }
        } catch (SQLException e) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Erreur lors de la récupération du niveau du joueur " + playerName + " : " + e.getMessage());
                e.printStackTrace();
            }
        }

        return 0;
    }

    public int getCurrentXP(String playerName, String jobType) {
        if (connection == null) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Connexion à la base de données non disponible.");
            }
            return 0;
        }

        if (!jobType.equals("farmer_xp") && !jobType.equals("hunter_xp")) {
            if (isDebugMode()) {
                plugin.getLogger().warning("Type de job invalide : " + jobType);
            }
            return 0;
        }

        String query = "SELECT " + jobType + " FROM player_jobs WHERE player_name = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(jobType);
            } else {
                if (isDebugMode()) {
                    plugin.getLogger().info("Le joueur " + playerName + " n'a pas été trouvé dans la base de données.");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération de l'XP du joueur " + playerName + " : " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public int getXPForNextLevel(int currentLevel) {
        return LevelCalculator.getXPForNextLevel(currentLevel);
    }

    public int getPlayerTier(String playerName, String jobType) {
        if (connection == null) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Connexion à la base de données non disponible.");
            }
            return 0;
        }

        if (!jobType.equals("farmer") && !jobType.equals("hunter")) {
            if (isDebugMode()) {
                plugin.getLogger().warning("Type de job invalide : " + jobType);
            }
            return 0;
        }

        String query = "SELECT " + jobType + "_tier FROM player_jobs WHERE player_name = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(jobType + "_tier");
            } else {
                if (isDebugMode()) {
                    plugin.getLogger().info("Le joueur " + playerName + " n'a pas été trouvé dans la base de données.");
                }
            }
        } catch (SQLException e) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Erreur lors de la récupération du palier du joueur " + playerName + " : " + e.getMessage());
                e.printStackTrace();
            }
        }

        return 0;
    }

    public PlayerJobData getPlayerJobData(String playerName, String jobType) {
        if (connection == null) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Connexion à la base de données non disponible.");
            }
            return null;
        }

        if (!jobType.equals("farmer") && !jobType.equals("hunter")) {
            if (isDebugMode()) {
                plugin.getLogger().warning("Type de job invalide : " + jobType);
            }
            return null;
        }

        String xpQuery = "SELECT " + jobType + "_xp, " + jobType + "_level, " + jobType + "_tier FROM player_jobs WHERE player_name = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(xpQuery)) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int currentXP = rs.getInt(jobType + "_xp");
                int level = rs.getInt(jobType + "_level");
                int tier = rs.getInt(jobType + "_tier");
                int nextXP = LevelCalculator.getXPForNextLevel(level);

                return new PlayerJobData(currentXP, level, nextXP, tier);
            } else {
                if (isDebugMode()) {
                    plugin.getLogger().info("Le joueur " + playerName + " n'a pas été trouvé dans la base de données.");
                }
            }
        } catch (SQLException e) {
            if (isDebugMode()) {
                plugin.getLogger().severe("Erreur lors de la récupération des données du joueur : " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean isDebugMode() {
        return plugin.getConfig().getBoolean("debug", false);
    }

    /*
    Changement de classe
     */
    public class PlayerJobData {
        private final int currentXP;
        private final int level;
        private final int nextXP;
        private final int tier;

        public PlayerJobData(int currentXP, int level, int nextXP, int tier) {
            this.currentXP = currentXP;
            this.level = level;
            this.nextXP = nextXP;
            this.tier = tier;
        }

        public int getCurrentXP() {
            return currentXP;
        }

        public int getLevel() {
            return level;
        }

        public int getNextXP() {
            return nextXP;
        }

        public int getTier() {
            return tier;
        }
    }
}

