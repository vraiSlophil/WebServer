# Serveur Web

## Description
Le Serveur Web est un serveur HTTP léger et performant construit en Java. Créé à l'occasion de la SAE 2.03 : Programmation d’un serveur Web configurable. 

## Fonctionnalités
- Sert des pages HTML statiques.
- Gère le contenu HTML dynamique à l'aide de balises personnalisées.
- Gère les fichiers multimédias, y compris les images, l'audio et la vidéo.
- Fournit des informations détaillées sur l'état du système.
- Gestion robuste des erreurs avec des pages d'erreur personnalisées.
- Contrôle d'accès basé sur l'IP.
- Système de journalisation détaillé.

## Développeurs
Les développeurs suivants ont contribué à ce projet :
- Nathan OUDER en S2D
- Nicolas SANJUAN en S2E

## Installation
Le serveur est empaqueté sous forme de fichier JAR. Pour exécuter le serveur, vous devez avoir Java installé sur votre machine.

1. Placez le fichier `WebServer.jar` dans le répertoire de votre choix. Par exemple, vous pouvez le placer dans `/usr/local/sbin/myweb`.

2. Naviguez jusqu'au répertoire où vous avez placé le fichier JAR. Par exemple :
   ```
   cd /usr/local/sbin/myweb
   ```

3. Exécutez le serveur en utilisant la commande suivante :
   ```
   java -jar WebServer.jar
   ```

Veuillez noter que vous devrez peut-être exécuter la commande avec `sudo` ou en tant qu'utilisateur root, en fonction des permissions de votre système.

## Exigences du système
- Système d'exploitation : Linux
- Environnement d'exécution Java (JRE) version 21 ou supérieure

## Support
Pour toute question ou préoccupation, veuillez contacter les développeurs.