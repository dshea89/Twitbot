# Twitbot
Automatically retrieve forex signals posted to Twitter and open orders as they come in

## Java

Requires the JTwitter Java library for accessing the Twitter API: http://www.winterwell.com/software/jtwitter.php

Retrieves forex signals posted to Twitter, parses them, and passes them to a file in a specified MetaTrader directory to be opened by the MetaTrader platform and parsed for trades.

The appropriate Twitter API information must be added to this code in the `<>` brackets (Line 64) in order to be properly used.

The "path" string variable (Line 23) must also be modified to point to the appropriate MetaTrader 4 "Files" directory. A sample has been provided to give an idea of the structure of the directory being sought.

Currently, signals from the following Twitter accounts are used:
+ @forex
+ @fxmgm

## MQL4

The provided MQL4 script is an Expert Advisor that is placed in the desired terminal's Experts folder. This script polls the output of the Java routine and places trades if new signals are found.