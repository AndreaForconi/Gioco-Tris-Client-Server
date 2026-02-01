# Descrizione di come avviare l'applicazione:

Per prima cosa per poter giocare a Tris bisogna avviare il ServerTris, dopo averlo fatto avviamo due volte ClientTris in due terminali differenti, a questo punto l'applicazione è pronta per essere utilizzata. 

## Descrizione di come provare l'applicazione:

Per provare l'applicazione basta scrivere "READY" in entrambe le console dei client così verremmo matchati per poter giocare contro. Solitamente il primo client che si connette sarà X ed il secondo sarà O, X parte sempre per primo. I client da qui possono giocare la loro partita. Ad ogni turno si deve scrivere al server la mossa che si vuole fare e quindi in quale posizione inserire il simbolo, in particolare si deve scrivere su ogni console di ciascun client, ogni volta che è il proprio turno, il numero che indica la posizione della griglia in cui inserire il simbolo. Specifichiamo che i numeri che riferiscono alla posizione in griglia sono così stabiliti: prima riga : 0, 1, 2 ; seconda riga: 3, 4, 5 ; terza riga: 6, 7, 8. Quindi ad esempio se vogliamo inserire X nella terza cella della seconda riga basta scrivere al server il numero 5. 
