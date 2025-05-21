package org.example.servermmr.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Data
@Entity
@Table(name = "battles")
public class Battle {
    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id", nullable = false)
    private Player player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id", nullable = false)
    private Player player2;

    @Column(nullable = false)
    private LocalDateTime startTime = LocalDateTime.now();

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private BattleResult result;

    @Column(nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_player_turn_id")
    private Player currentPlayerTurn;

    private int currentDigitIndex = 0;

    @ElementCollection
    @CollectionTable(name = "battle_player_digits", joinColumns = @JoinColumn(name = "battle_id"))
    @MapKeyJoinColumn(name = "player_id")
    @Column(name = "digits")
    private Map<Player, String> playerDigits = new HashMap<>();

    @Transient
    private Set<Player> digitFinishedThisRound = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private BattleStatus status = BattleStatus.ACTIVE;

    public Battle() {
        // Конструктор по умолчанию для JPA
    }

    public Battle(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayerTurn = player1;
    }

    // Остальные методы остаются практически без изменений,
    // но нужно адаптировать работу с playerDigits для хранения в БД

    public String rollDigit(Player player) {
        if (!player.equals(currentPlayerTurn)) {
            return "Сейчас не ваш ход!";
        }

        // Получаем текущие цифры игрока
        int[] digits = getDigitsForPlayer(player);

        // Генерируем случайную цифру (0-9)
        int digit = ThreadLocalRandom.current().nextInt(10);
        digits[currentDigitIndex] = digit;
        saveDigitsForPlayer(player, digits);

        // Помечаем, что игрок сделал ход в этом раунде
        digitFinishedThisRound.add(player);

        // Формируем сообщение о ходе
        StringBuilder sb = new StringBuilder();
        sb.append(player.getName())
                .append(" выбрал ")
                .append(digit)
                .append(" в разряде ")
                .append(getDigitName(currentDigitIndex))
                .append(".");

        // Проверяем, завершился ли раунд (оба игрока сделали ход)
        boolean roundFinished = digitFinishedThisRound.size() == 2;

        if (roundFinished) {
            digitFinishedThisRound.clear();
            currentDigitIndex++;

            // Проверяем, завершилась ли игра (все 3 раунда)
            if (currentDigitIndex > 2) {
                // Получаем цифры обоих игроков
                int[] p1Digits = getDigitsForPlayer(player1);
                int[] p2Digits = getDigitsForPlayer(player2);

                // Конвертируем в числа
                int n1 = toNumber(p1Digits);
                int n2 = toNumber(p2Digits);

                // Определяем результат
                if (n1 > n2) {
                    result = BattleResult.P1_WIN;
                } else if (n2 > n1) {
                    result = BattleResult.P2_WIN;
                } else {
                    result = BattleResult.DRAW;
                }

                // Завершаем бой
                endTime = LocalDateTime.now();
                isActive = false;
                status = BattleStatus.FINISHED;

                // Добавляем итоговое сообщение
                sb.append("\nИгра завершена! ").append(getResultMessage());
                return sb.toString();
            }
        }

        // Передаем ход другому игроку
        switchTurn(player);
        return sb.toString();
    }

    private int toNumber(int[] digits) {
        if (digits == null || digits.length != 3) {
            return 0;
        }
        return digits[2] * 100 + digits[1] * 10 + digits[0];
    }

    /**
     * Возвращает название разряда по индексу
     */
    private String getDigitName(int index) {
        return switch (index) {
            case 0 -> "единицы";
            case 1 -> "десятки";
            case 2 -> "сотни";
            default -> "неизвестный разряд";
        };
    }
    private int[] getDigitsForPlayer(Player player) {
        String digitsStr = playerDigits.getOrDefault(player, "0,0,0");
        return Arrays.stream(digitsStr.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private void saveDigitsForPlayer(Player player, int[] digits) {
        String digitsStr = digits[0] + "," + digits[1] + "," + digits[2];
        playerDigits.put(player, digitsStr);
    }

    private void switchTurn(Player current) {
        currentPlayerTurn = current.equals(player1) ? player2 : player1;
    }

    private String getResultMessage() {
        int[] p1Digits = getDigitsForPlayer(player1);
        int[] p2Digits = getDigitsForPlayer(player2);
        int n1 = toNumber(p1Digits);
        int n2 = toNumber(p2Digits);

        String winner;
        if (n1 > n2) {
            winner = player1.getName();
        } else if (n2 > n1) {
            winner = player2.getName();
        } else {
            winner = "Ничья";
        }

        return String.format("%s: %03d, %s: %03d. Победил: %s",
                player1.getName(), n1,
                player2.getName(), n2,
                winner);
    }



    public enum BattleResult {
        P1_WIN,
        P2_WIN,
        DRAW,
        ABORTED
    }

    public enum BattleStatus {
        ACTIVE,
        FINISHED,
        ABORTED
    }
}