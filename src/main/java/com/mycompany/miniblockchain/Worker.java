/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.miniblockchain;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author vboxuser
 */
public class Worker {

    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //Comprobacion de numero de args
        if (args.length != 4) {
            System.out.println("El numero de argumentos es incorrecto");
            return;
        }

        //Recibimos  los dato del principal
        String Hash = args[0];
        String datos = args[1];
        int dificultad = Integer.parseInt(args[2]);
        int id = Integer.parseInt(args[3]);

        String ceros = "";
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        //calculamos los ceros que debe de tener segun la dificultad
        for (int i = 0; i < dificultad; i++) {
            ceros = ceros + "0";
        }

        long nonce = id;

        //Calculo de huella digital y mostramos resultado
        while (true) {
            String texto = Hash + "|" + datos + "|" + nonce;

            byte[] hash = md.digest(texto.getBytes("UTF-8"));

            String resultado = "";

            for (byte b : hash) {
                resultado += String.format("%02x", b);
            }

            if (resultado.startsWith(ceros)) {
                System.out.println("resultado|" + nonce + "|" + resultado + "|" + id);
                return;
            }

            nonce = nonce + 4; //Para no repetir trabajo
        }

    }

}
