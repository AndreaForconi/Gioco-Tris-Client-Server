import java.io.*;
import java.net.*;
import java.util.*;


public class ServerTris {

	// porta su cui il server rimane in ascolto
    static final int PORTA = 12345;

    // metodo per avviare il server
    public void avvia() throws Exception {

    	// creazione del server
        ServerSocket server = new ServerSocket(PORTA); 
        System.out.println("Server avviato sulla porta " + PORTA);

        //ciclo infinito per accettare le partite
        while (true) {

            System.out.println("Attendo giocatore 1...");
            //accettazione del client
            Giocatore g1 = new Giocatore(server.accept());
            //aspettiamo che scriva READY, nel caso il server accetta più client crea un oggetto giocatore per ogni client che si connette e aspetta i primi due che mettono pronto, gli altri sono sospesi dal main Thread
            aspettaReady(g1);

            System.out.println("Attendo giocatore 2...");
            Giocatore g2 = new Giocatore(server.accept());
            aspettaReady(g2);
            
            // la partita è gestita da un Thread separato rispetto al main Thread
            new Thread(new Partita(g1, g2)).start();
        }
    }

    // metodo per chiedere al giocatore di scrivere READY
    void aspettaReady(Giocatore g) throws Exception {
        g.invia("Scrivi READY per iniziare");

        while (true) {
            String s = g.leggi();
            // READY può essere scritto sia maiuscolo sia minuscolo
            if (s != null && s.equalsIgnoreCase("READY"))
                break;
        }
    }

    // classe per il giocatore
    static class Giocatore {

        Socket socket;
        BufferedReader in;
        PrintWriter out;
        
        // costruttore della classe
        Giocatore(Socket s) throws Exception {
            socket = s;
            // legge messaggi dal client
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            // invia messaggi al client
            out = new PrintWriter(s.getOutputStream(), true);
        }
        
        // metodo per leggere un messaggio dal client
        String leggi() throws Exception {
            return in.readLine();
        }

        // invia un messaggio al client
        void invia(String msg) {
            out.println(msg);
        }
    }

    // classe per gestire la partita attraverso il Thread
    static class Partita implements Runnable {
    	
        Giocatore g1, g2;
        // griglia tris 3x3
        char[] griglia = new char[9];
        
        // giocatori di appoggio che indicano il giocatore di turno e l'avversario che ci servono per capire a chi sta e il suo avversario
        Giocatore turno, altro;
        char simboloTurno;

        // costruttore della classe
        Partita(Giocatore a, Giocatore b) {
            g1 = a;
            g2 = b;
            //inizializzazione della griglia con "-"
            for (int i = 0; i < 9; i++)
                griglia[i] = '-';
        }

        public void run() {

            try {

                Random r = new Random();

                Giocatore giocatoreX;
                Giocatore giocatoreO;
                // assegnamento casuale di X e O
                if (r.nextBoolean()) {
                    giocatoreX = g1;
                    giocatoreO = g2;
                } else {
                    giocatoreX = g2;
                    giocatoreO = g1;
                }
                // comunicazione dei simboli ai giocatori
                giocatoreX.invia("SEI X (inizi tu)");
                giocatoreO.invia("SEI O");
                //essendo che X inizia per primo il giocatore di turno sarà X e il suo avversario O
                turno = giocatoreX;
                altro = giocatoreO;
                simboloTurno = 'X';
                //invio della griglia
                inviaGriglia();

                // ciclo per la partita
                while (true) {
                	// avviso il client di turno che sta a lui
                    turno.invia("TOCCA A TE");
                    // avviso al client avversario che deve aspettare
                    altro.invia("ATTENDI TURNO");
                    //leggo la mossa del giocatore di turo
                    int mossa = leggiMossa(turno);
                    //aggiornamento della griglia
                    griglia[mossa] = simboloTurno;
                    //invio della griglia aggiornata ad entrambi
                    inviaGriglia();
                    
                    // controllo della vittoria 
                    if (controllaVittoria(simboloTurno)) {
                        turno.invia("HAI VINTO!");
                        altro.invia("HAI PERSO!");
                        break;
                    }
                    // controllo del pareggio
                    if (controllaPareggio()) {
                        g1.invia("PAREGGIO");
                        g2.invia("PAREGGIO");
                        break;
                    }

                    // switch del turno 
                    Giocatore tmp = turno;
                    turno = altro;
                    altro = tmp;
                    // cambio del simbolo turno, qui viene cambiato il simbolo del giocatore che deve fare la mossa, così da alternare il turno tra X e O
                    simboloTurno = (simboloTurno == 'X') ? 'O' : 'X';
                }
                // chiusura delle socket alla fine 
                g1.socket.close();
                g2.socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // metodo per leggere una mossa valida dal giocatore 
        int leggiMossa(Giocatore g) throws Exception {
            while (true) {
                String msg = g.leggi();
                // controllo che il messaggio del client non sia vuoto e che inizi con "MOVE"
                if (msg != null && msg.startsWith("MOVE")) {
                	// converte la stringa msg in un intero dopo aver diviso in un array il messaggio ricevuto, questa riga permette di estrarre la posizione scelta dal giocatore
                    int pos = Integer.parseInt(msg.split(" ")[1]);
                    // controllo della validità della mossa
                    if (pos >= 0 && pos < 9 && griglia[pos] == '-')
                        return pos;
                    // se la posizione non è valida invia un messaggio al client di avviso
                    g.invia("Mossa non valida");
                }
            }
        }
        // metodo per inviare la griglia al client 
        void inviaGriglia() {
            // creo una stringa vuota che conterrà lo stato della griglia definito da una stringa con i simboli di tutte le celle separate da virgole
            String stato = "";

            // ciclo su tutti gli indici dell'array griglia
            for (int i = 0; i < griglia.length; i++) {
                // aggiungo il simbolo della cella + una virgola
                stato += griglia[i] + ",";
            }

            // tolgo l'ultima virgola in eccesso
            stato = stato.substring(0, stato.length() - 1);

            // invio lo stato della griglia a entrambi i giocatori
            g1.invia("BOARD " + stato);
            g2.invia("BOARD " + stato);
        }


        boolean controllaPareggio() {
            // ciclo su tutte le celle della griglia usando un for classico
            for (int i = 0; i < griglia.length; i++) {
                // se c'è almeno una cella vuota, non c'è pareggio
                if (griglia[i] == '-') {
                    return false;
                }
            }
            // se tutte le celle sono occupate, allora c'è pareggio
            return true;
        }


        boolean controllaVittoria(char s) {
            // matrice delle combinazioni vincenti (righe, colonne, diagonali)
            int[][] comb = {
                {0,1,2},{3,4,5},{6,7,8},  // righe
                {0,3,6},{1,4,7},{2,5,8},  // colonne
                {0,4,8},{2,4,6}           // diagonali
            };

            // ciclo classico sulle combinazioni vincenti
            for (int i = 0; i < comb.length; i++) {
                // prendo i tre indici della combinazione corrente
                int a = comb[i][0];
                int b = comb[i][1];
                int c = comb[i][2];

                // controllo se tutte e tre le celle contengono il simbolo s
                if (griglia[a] == s && griglia[b] == s && griglia[c] == s) {
                    return true; // vittoria trovata
                }
            }

            // se nessuna combinazione è vincente, ritorno false
            return false;
        }

    }
}