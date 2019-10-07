package IA.java;

import IA.Bicing.Estaciones;

import java.util.Random;

// TODO: VERSION 2: generar problemas aleatorios (numEstaciones, posicionesEstaciones, numBicisEstacion, numFurgonetas,
// TODO: tipo demanda, semilla aleatoria)
public class BicingProblem {
    // VERSION 1: para un numero fijado de datos iniciales (ACTUAL)

    public static void main(String[] args) {
        Random random = new Random();

        int numEstaciones = 3;
        int numBicis = 150;
        int numFurgonetas = 2;
        int demanda = 0;                // tipo demanda equilibrada
        int seed = random.nextInt();

        BicingSolution solucionInicial = new BicingSolution(numEstaciones, numBicis, demanda, seed, numFurgonetas);
        solucionInicial.generarSolucionInicialRandom();

        printAsignaciones(solucionInicial);
        printCapacidadesEstacionesInicial(solucionInicial);
        printDemanda(solucionInicial);
        printCapacidadesEstaciones(solucionInicial);
        printCapacidadesFurgonetas(solucionInicial);
        printDestinos(solucionInicial);
    }

    private static void printAsignaciones(BicingSolution solucion) {
        System.out.println(" ***** ASIGNACIONES ***** ");
        int[] asignaciones = solucion.getAsignaciones();

        for (int i = 0; i < asignaciones.length; ++i) {
            System.out.println("Furgoneta #" + i);
            System.out.println("Estacion origen = " + asignaciones[i]);
        }
        System.out.println(" ***** FIN ***** ");
        System.out.println(" ");
    }

    private static void printCapacidadesEstacionesInicial(BicingSolution solucion) {
        System.out.println(" ***** CAPACIDADES ESTACIONES INICIAL ***** ");
        Estaciones estaciones = solucion.getEstaciones();

        for (int i = 0; i < estaciones.size(); ++i) {
            System.out.println("Estacion #" + i);
            System.out.println("Capacidad = " + estaciones.get(i).getNumBicicletasNext());
        }
        System.out.println(" ***** FIN ***** ");
        System.out.println(" ");
    }

    private static void printDemanda(BicingSolution solucion) {
        System.out.println(" ***** DEMANDAS ***** ");
        Estaciones estaciones = solucion.getEstaciones();

        for (int i = 0; i < estaciones.size(); ++i) {
            System.out.println("Estacion #" + i);
            System.out.println("Demanda = " + estaciones.get(i).getDemanda());
        }
        System.out.println(" ***** FIN ***** ");
        System.out.println(" ");
    }

    private static void printCapacidadesEstaciones(BicingSolution solucion) {
        System.out.println(" ***** CAPACIDADES ESTACIONES ***** ");
        int[] capacidadEstaciones = solucion.getCapacidadEstaciones();

        for (int i = 0; i < capacidadEstaciones.length; ++i) {
            System.out.println("Estacion #" + i);
            System.out.println("Capacidad = " + capacidadEstaciones[i]);
        }
        System.out.println(" ***** FIN ***** ");
        System.out.println(" ");
    }

    private static void printCapacidadesFurgonetas(BicingSolution solucion) {
        System.out.println(" ***** CAPACIDADES FURGONETAS ***** ");
        int[] capacidadFurgonetas = solucion.getCapacidadFurgonetas();

        for (int i = 0; i < capacidadFurgonetas.length; ++i) {
            System.out.println("Furgoneta #" + i);
            System.out.println("Capacidad = " + capacidadFurgonetas[i]);
        }
        System.out.println(" ***** CAPACIDADES FURGONETAS ***** ");
        System.out.println(" ");
    }

    private static void printDestinos(BicingSolution solucion) {
        System.out.println(" ***** DESTINOS ***** ");
        int[][] destinos = solucion.getDestinos();

        for (int i = 0; i < destinos.length; ++i) {
            System.out.println("Furgoneta #" + i);
            System.out.println("Destino 1 = " + destinos[i][0]);
            System.out.println("Destino 2 = " + destinos[i][1]);
        }
        System.out.println(" ***** FIN ***** ");
        System.out.println(" ");
    }
}