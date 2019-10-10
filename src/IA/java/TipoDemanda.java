package IA.java;

public enum TipoDemanda {
    EQUILIBRADA, PUNTA;

    /**
     * Devuelve el número entero que codifica el tipo de demanda 'tipoDemanda'
     *
     * @param tipoDemanda tipo de la demanda
     * @return el número entero que codifica el tipo de demanda
     */
    public static int getCode(TipoDemanda tipoDemanda) {
        if (tipoDemanda.equals(EQUILIBRADA)) {
            return 0;
        } else { // PUNTA
            return 1;
        }
    }
}
