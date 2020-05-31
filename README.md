# Little Printer

Start server
```
sbt run
```

Start UI
```
cd ui
yarn start
```

Open [http://localhost:3000/](http://localhost:3000/)

* It might take up to 33 seconds to find an unclaimed printer.
* It might take up to 10 seconds to find an already claimed printer.

## Setup your Little Printer

I assume that you already followed the steps outlined at

  https://github.com/genmon/sirius/wiki/Updating-the-Bridge

Find your local network IP

```
./my_ip.sh
```

Find your printer.

```
# requires nmap
./find_brige.sh
```

Open `http://<PRINTER-IP>:81/configure`

* username `berg`
* password `hereandthere`

Change the server to `http://<MY-LOCAL-IP>:8000`

