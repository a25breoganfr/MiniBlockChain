/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.miniblockchain;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author vboxuser
 */
public class Principal {

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {

        Scanner sc = new Scanner(System.in);

        int dificultad = 3;

        //Validar dificultad
        if (dificultad < 1 || dificultad > 64) {
            System.out.println("Ls dificultad no esta entre los valores 1 y 64");
            return;
        }

        System.out.println("Introduce la semilla inicial");
        String semilla = sc.nextLine();
        //Validar semilla
        if (semilla.trim().isEmpty()) {
            System.out.println("La semilla esta vacia");
            return;

        }
        System.out.println("Introduce la ruta del archivo");
        String ruta = sc.nextLine();

        //Lectura de los bloques del archivo
        List<String> bloques = new ArrayList<>();
        BufferedReader lectorArchivo;

        //Validacion de ruta
        try {
            lectorArchivo = new BufferedReader(new FileReader(ruta));
        } catch (FileNotFoundException e) {
            System.out.println("La ruta no es correcta");
            return;
        }

        String lineaArchivo;

        while ((lineaArchivo = lectorArchivo.readLine()) != null) {
            if (!lineaArchivo.trim().isEmpty()) {
                bloques.add(lineaArchivo);
            }
        }

        lectorArchivo.close();

        //Validacion de bloques
        if (bloques.isEmpty()) {
            System.out.println("El archivo no contiene bloques");
            return;
        }

        System.out.println("Numero de bloques: " + bloques.size());//Prueba

        //Calcula el hash inicial a partir de la semilla
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(semilla.getBytes("UTF-8"));
        String hashAnterior = "";

        for (byte b : hashBytes) {
            hashAnterior += String.format("%02x", b);
        }

        System.out.println("Hash inicial: " + hashAnterior);

        int numBloque = 1;
        //Bucle por cada Bloque
        for (String datos : bloques) {
            System.out.println("\n======================================");
            System.out.println("          BLOQUE: " + numBloque);
            System.out.println("\n======================================");
            System.out.println("Datos del bloque: " + datos);
            System.out.println("Hash anterior: " + hashAnterior);
            System.out.println("Dificultad: " + dificultad);

            int numProcesos = 4;

            List<Process> procesos = new ArrayList<>();
            List<BufferedReader> lectores = new ArrayList<>();

            int ganador = -1;
            String hashGanador = "";

            try {//Controlamos errores inesperados
                for (int i = 0; i < numProcesos; i++) {
                    String cp = System.getProperty("java.class.path");

                    ProcessBuilder pb = new ProcessBuilder(
                            "java", "-cp", cp,
                            "com.mycompany.miniblockchain.Worker",
                            hashAnterior, datos, String.valueOf(dificultad), String.valueOf(i));
                    pb.redirectErrorStream(true);

                    Process p = pb.start();

                    procesos.add(p);
                    lectores.add(new BufferedReader(new InputStreamReader(p.getInputStream())));
                }

                while (ganador == -1) {

                    for (BufferedReader br : lectores) {

                        if (br.ready()) {
                            String line = br.readLine();

                            String[] partes = line.split("\\|");

                            if (partes.length == 4) {
                                //Mostramos las partes de los Workers. Con break solo del gandor
                                long nonce = Long.parseLong(partes[1]);
                                String hashDevuelto = partes[2];
                                int id = Integer.parseInt(partes[3]);

                                System.out.println("Worker con id: " + id);
                                System.out.println("Nonce: " + nonce);
                                System.out.println("Hash devuelto: " + hashDevuelto);

                                //Reconstruimos el hash para comprobar que coincide con el del Worker
                                String texto = hashAnterior + "|" + datos + "|" + nonce;

                                md = MessageDigest.getInstance("SHA-256");
                                byte[] hashComprobar = md.digest(texto.getBytes("UTF-8"));
                                String hashCalculado = "";

                                for (byte b : hashComprobar) {
                                    hashCalculado += String.format("%02x", b);
                                }
                                //Imprimimos el hash comprobado por el Principal
                                System.out.println("Hash recalculado por Principal:  " + hashCalculado);

                                //Comprobamos que coincidan los dos hash y que la dificultad sea correcta
                                if (hashCalculado.equals(hashDevuelto) && hashCalculado.startsWith("0".repeat(dificultad))) {
                                    System.out.println("El resultado es valido");
                                    ganador = id;
                                    hashGanador = hashCalculado;
                                    System.out.println("El ganador es: " + ganador + " con PID: " + procesos.get(ganador).pid());
                                    break;
                                } else {
                                    System.out.println("El resultado no es valido");
                                }

                            }
                        }

                    }

                }

            } finally {//Controlamos errores inesperados
                //Detenemos los procesos que no son el ganador
                for (Process proceso : procesos) {
                    if (proceso.isAlive() && (ganador == -1 || proceso.pid() != procesos.get(ganador).pid())) {
                        proceso.destroy();
                    }
                }

                //Esperamos a que todos los procesos terminen
                for (Process proceso : procesos) {
                    proceso.waitFor();
                    //Salidas para comprobaciones
                    //System.out.println("Worker Terminado");
                    //System.out.println("codigo:" + proceso.exitValue()); 
                }

                System.out.println("Todos los Workers han terminado");

            }

            hashAnterior = hashGanador;
            numBloque++;
        }

    }
}
