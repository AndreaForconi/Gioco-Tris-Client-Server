import java.io.*;
import java.net.*;
import java.util.*;


public class ClientTris {

    public void avvia() throws Exception {
    	
    	// collegamento al server
        Socket socket = new Socket("localhost", 12345);
        
        // in e out utilizzato per leggere e scrivere sulla socket col server
        BufferedReader in =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));

        PrintWriter out =
                new PrintWriter(socket.getOutputStream(), true);
        
        Scanner tastiera = new Scanner(System.in);

        System.out.println("Connesso al server!");
        System.out.println("Scrivi READY:");

        // legge READY da tastiera e invia READY al server
        tastiera.nextLine();
        out.println("READY");

        //ciclo per ricevere i messaggi dal server
        while (true) {
        	
            String msg = in.readLine();
            // se il messaggio è uguale a null significa che il server è chiuso
            if (msg == null) break;

            if (msg.startsWith("BOARD")) {
            	// chiama il metodo per stampare la griglia sul terminale invocando il metodo split che permette di stampare direttamente la griglia aggiornata senza "BOARD"
                stampaGriglia(msg.split(" ")[1]); 
            }
            // quando tocca al client chiede all'utente di inserire la posizione e la invia al server
            else if (msg.equals("TOCCA A TE")) {
                System.out.print("Posizione (0-8): ");
                int m = tastiera.nextInt();
                out.println("MOVE " + m);
            }
            // esce se la partita è finita
            else if (msg.contains("VINTO") ||
                     msg.contains("PERSO") ||
                     msg.contains("PAREGGIO")) {

                System.out.println(msg);
                break;
            }
            else {
                System.out.println(msg);
            }
        }
        // chiusura della socket
        socket.close();
    }

    // metodo per stampare la griglia
    void stampaGriglia(String stato) {
    	
    	// divide la stringa in un array che si basa sulla griglia ricevuta dal server
        String[] b = stato.split(",");

        System.out.println();

        for (int i = 0; i < 9; i++) {
        	// se la cella è vuota mostra il numero della cella altrimenti mostra X o O
            String cella = b[i].equals("-") ? "" + i : b[i];
            // stampa il contenuto della cella con uno spazio
            System.out.print(cella + " ");
            //ogni 3 celle va a capo così si crea la griglia 3x3
            if (i % 3 == 2) System.out.println();
        }
    }
}
