package com.abfann.libunand.data;

public interface IPlayerEconomy {

    /**
     * Obtiene el balance actual del jugador
     */
    int getBalance();

    /**
     * Establece el balance del jugador
     */
    void setBalance(int balance);

    /**
     * Añade JoJoCoins al balance
     */
    void addBalance(int amount);

    /**
     * Resta JoJoCoins del balance
     * @return true si la operación fue exitosa, false si no hay suficientes fondos
     */
    boolean removeBalance(int amount);

    /**
     * Verifica si el jugador tiene suficiente balance
     */
    boolean hasBalance(int amount);

    /**
     * Transfiere dinero a otro jugador
     * @return true si la transferencia fue exitosa
     */
    boolean transferTo(IPlayerEconomy target, int amount);
}