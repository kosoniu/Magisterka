public interface PawnInterface {
    /**
     * Metoda zwraca identyfikator pionka
     *
     * @return unikalny identyfikator pionka
     */
    public int getID();

    /**
     * Zlecenie przesuniÄcia pionka w lewo (dekrementacja indeksu kolumny)
     * Metoda blokuje wÄtek, ktĂłry jÄ wykonaĹ na czas realizacji
     * zleconej operacji.
     *
     * @return metoda zwraca indeks kolumny, w ktĂłrej ruch siÄ zakoĹczyĹ.
     */
    public int moveLeft();

    /**
     * Zlecenie przesuniÄcia pionka w prawo (inkrementacja indeksu kolumny)
     * Metoda blokuje wÄtek, ktĂłry jÄ wykonaĹ na czas realizacji
     * zleconej operacji.
     *
     * @return metoda zwraca indeks kolumny, w ktĂłrej ruch siÄ zakoĹczyĹ.
     */
    public int moveRight();

    /**
     * Zlecenie przesuniÄcia pionka w gĂłrÄ (inkrementacja indeksu wiersza).
     * Metoda blokuje wÄtek, ktĂłry jÄ wykonaĹ na czas realizacji
     * zleconej operacji.
     *
     * @return metoda zwraca indeks wiersza, w ktĂłrej ruch siÄ zakoĹczyĹ.
     */
    public int moveUp();

    /**
     * Zlecenie przesuniÄcia pionka w dĂłĹ (dekrementacja indeksu wiersza).
     * Metoda blokuje wÄtek, ktĂłry jÄ wykonaĹ na czas realizacji
     * zleconej operacji.
     *
     * @return metoda zwraca indeks wiersza, w ktĂłrej ruch siÄ zakoĹczyĹ.
     */
    public int moveDown();

    /**
     * Rejestracja wÄtku, ktĂłry bÄdzie wykonywaĹ operacje na tym pionku. Rejestracja
     * musi zostaÄ dokonana przed pierwszym zleceniem ruchu pionka.
     *
     * @param thread wÄtek odpowiedzialny za ruch pionka.
     */
    public void registerThread(Thread thread);
}