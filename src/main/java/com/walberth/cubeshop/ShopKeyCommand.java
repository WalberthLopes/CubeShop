package com.walberth.cubeshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ShopKeyCommand implements CommandExecutor {

    private final Main main;

    public ShopKeyCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            String playerName = player.getName();
            String prefix = main.getConfig().getString("Prefix");

            if (prefix == null) prefix = ChatColor.DARK_PURPLE + "[CubeCave] ";
            String finalPrefix = prefix;

            if (args.length == 0) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', finalPrefix) +
                        ChatColor.GRAY + "Utilize o comando: /shopkey <key> para ativar uma key.");
                return false;
            } else {
                String playerSendedKey = args[0];
                String postgres_url = main.getConfig().getString("Postgres_URL");

                try {
                    assert postgres_url != null;
                    Connection connection = DriverManager.getConnection(postgres_url);

                    String SQL_SELECT_KEY = "SELECT * FROM PURCHASES WHERE CODE = ?";
                    String SQL_SELECT_COMMANDS = "SELECT COMMANDS FROM PRODUCTS WHERE NAME = ?";
                    String SQL_UPDATE_KEY = "UPDATE PURCHASES SET USED = ?, USERNAME = ? WHERE CODE = ?";

                    PreparedStatement statement = connection.prepareStatement(SQL_SELECT_KEY);
                    statement.setString(1, playerSendedKey);

                    ResultSet resultSet = statement.executeQuery();

                    if (!resultSet.isBeforeFirst()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', finalPrefix) +
                                ChatColor.RED + "Key não encontrada. Verifique sua key e tente novamente.");
                    }

                    while (resultSet.next()) {
                        String productName = resultSet.getString("product");
                        boolean productUsed = resultSet.getBoolean("used");

                        if (productUsed) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', finalPrefix) +
                                    ChatColor.RED + "Esta key já foi utilizada não está mais disponível.");
                        }else {
                            statement = connection.prepareStatement(SQL_UPDATE_KEY);
                            statement.setBoolean(1, true);
                            statement.setString(2, playerName);
                            statement.setString(3, playerSendedKey);
                            statement.execute();

                            statement = connection.prepareStatement(SQL_SELECT_COMMANDS);
                            statement.setString(1, productName);
                            resultSet = statement.executeQuery();

                            while (resultSet.next()) {
                                String allCommands = resultSet.getString("commands").replace("<player>", playerName);
                                String[] mapCommands = allCommands.split(",");
                                List<String> commands = Arrays.asList(mapCommands);

                                String messageToAll = main.getConfig().getString("Message_all");

                                if (messageToAll == null) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', finalPrefix) +
                                            ChatColor.GREEN + "Sua key foi ativada com sucesso! Obrigado pelo apoio!");

                                    Bukkit.getOnlinePlayers().forEach(user ->
                                            user.sendMessage(ChatColor.translateAlternateColorCodes('&', finalPrefix) +
                                                    ChatColor.YELLOW + playerName +
                                                    " " +
                                                    ChatColor.WHITE + "Passou na CubeShop e levou um " +
                                                    ChatColor.YELLOW + productName +
                                                    ChatColor.WHITE + "! " +
                                                    "Obrigado pelo seu apoio!"));

                                    commands.forEach(cmd -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd));
                                } else {
                                    String formatedMessage = messageToAll
                                            .replace("<cubeshop_product_name>", productName)
                                            .replace("<cubeshop_player_name>", playerName);

                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', finalPrefix) +
                                            ChatColor.GREEN + "Sua key foi ativada com sucesso! Obrigado pelo apoio!");

                                    Bukkit.getOnlinePlayers().forEach(user ->
                                            user.sendMessage(ChatColor.translateAlternateColorCodes('&', finalPrefix) +
                                                    " " +
                                                    ChatColor.translateAlternateColorCodes('&', formatedMessage)));

                                    commands.forEach(cmd -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd));
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }
}
