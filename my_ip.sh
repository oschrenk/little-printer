#!/bin/bash -eu
INTERFACE=${1-}
if [ -z "$INTERFACE" ]
then
  INTERFACE=$(route get google.com | grep interface | awk -F  ": " '{print $2}')
fi

IP=$(ifconfig $INTERFACE inet | grep inet | awk '{print $2}')

echo "$IP"
