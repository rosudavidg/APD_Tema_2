```
=========================================================================================
Nume:    Rosu
Prenume: Gabriel - David
Grupa:   331CC
Data:    25.11.2018
Materie: APD, anul 3, semestrul 1
=========================================================================================
                                         Tema 2
                             The miners and the sleepy wizards
=========================================================================================
    CUPRINS
-----------------------------------------------------------------------------------------
    [1] INTRODUCERE
    [2] CUM? DE CE?
    [3] FEEDBACK
=========================================================================================
[1] INTRODUCERE
-----------------------------------------------------------------------------------------
    Tema reprezinta rezolvarea problemei de tip multiple producer-consumer in
    limbajul Java (v7).
    https://acs.curs.pub.ro/2018/pluginfile.php/42092/mod_resource/content/7/
APD___Tema_2.pdf
    
    Concret, minerii sunt muncitorii care apeleaza o functie "grea" din punct de vedere
computational (hash recursiv de un numar mare de ori), iar vrajitorii verifica daca
aceste chei sunt bune si dau mai departe spre rezolvare alte siruri.

    Pe checker tema a fost punctata cu 100 de puncte.
    Local, timpii de executie sunt buni, iar solutia scaleaza.

    In rezolvare am folosit clase din java.util.concurrent.
    Nu am modificat semnaturile functiilor.
=========================================================================================
[2] CUM? DE CE?
=========================================================================================
-----------------------------------------------------------------------------------------
    Canalul de comunicatie dintre vrajitori si mineri este format din doua canale:
un canal WizardChannel, unde vrajitorii scriu, iar minerii citesc si un canal
MinerChannel, unde minerii scriu, iar vrajitorii citesc.

    Pentru a sincroniza WizardChannel, am folosit doua semafoare:
WizardChannelReadSemaphore si WizardChannelWriteSemaphore. Primul este initializat
cu 0, iar al doilea cu 1.

    Aceste mesaje sunt pastrare intr-un HashMap. Atunci cand un vrajitor scrie,
semaforul Write se va bloca, pana cand acesta termina de adaugat mesajul in hashMap.
    
    Un miner poate lua mesajul complet doar doua "mesaje" de la un vrajitor.
Deci, la fiecare doua mesaje primite, bariera pentru citire va permite inca un
cititor. Nu este nevoie de sincronizare a cititorilor aici, deoarece ei sunt
sincronzati in clasa Miner, in functia run. Un singur cititor preia ambele mesaje,
unul dupa altul.

    HashMap-ul precizat anterior are cheile id-ul thread-urilor vrajitorilor,
adaugate pe parcursul executiei, folosind Thread.currentThread().getId(). Valoarea
unui element este o lista ArrayList<Message>. Noile mesaje sunt adaugate unul dupa
altul in aceasta lista, in functie de vrajitorul care le-a initiat.

    Variabila nNewMessages se incrementeaza la fiecare mesaj nou primit. Cand
aceasta ajunge la doi, bariera de citire va permite inca unui miner sa citeasca,
iar nNewMessages revine la 0.

    Variabila minerIsReading este true, daca un miner a luat primul mesaj din hashMap,
iar acum il asteapta pe al doilea. La citirea primului mesaj, aceasta devine true, iar
dupa citirea celui de-al doilea, devine false. In acest fel reusesc sa trimit mesaje
aceluiasi miner. Pentru a trimite mesajele potrivite, folosesc minerIsReadingFrom,
care indica cheia din hashMap de unde trebuie sa iau urmatorul mesaj, adica de la ce
vrajitor am luat primul mesaj, de acolo il voi lua si pe al doilea.

    Pentru sincronizarea canalului de comunicatie MinerChannel am folosit BlockingQueue.
Cand vine un mesaj de la un miner, il adaug. Cand un wizard citeste, extrag mesajul.

    In clasa Miner, am copiat cele doua functii din schelet pentru aplicarea hash-ului.
    Functia run are un while infinit. Dupa ultimul update, minerii vor fi inchisi
automat, deci nu am conditie de iesire.
    Sincronizarea este facuta cu un singur semafor.

    - se blocheaza semaforul
        - se citesc cele doua mesaje
        - se verifica daca camera este rezolvata sau nu (daca este, se deblocheaza
semaforul si se continua cu urmatoarea iteratie a while-ului)
        - se adauga camera in lista celor rezolvate (pentru a nu fi calculata de un alt
miner in paralel)
    - se elibereaza semaforul
    - se calculeaza hash-ul (mai multi miner calculeaza hash-uri in paralel, deoarece
nu exista nicio restrictie de sincronizare aici)
    - se trimite raspunsul pe canalul de comunicatie catre wizard.

=========================================================================================
[3] FEEDBACK
-----------------------------------------------------------------------------------------
[+]     Tema isi indeplineste cu succes scopul, acela de a pune in practica cunostintele
acumulate de-alungul cursurilor, laboratoarelor, dar si studiului individual.
=========================================================================================
                                        SFARSIT
=========================================================================================
```