package main;

public interface OptimizerInterface {

    /**
     * Metoda umoĹźliwia przekazanie planszy z pionkami
     *
     * @param board plansza
     */
    public void setBoard( BoardInterface board );

    /**
     * Wstrzymanie pracy wÄtkĂłw. Po zakoĹczeniu metody
     * Ĺźaden z pionkĂłw nie moĹźe byÄ w tracie zmiany poĹoĹźenia.
     */
    public void suspend();

    /**
     * Kontynuacja pracy wÄtkow.
     */
    public void resume();

}