#!/bin/bash

if [ $# -ne 1 ]; then
  echo "usage: [property]"
  exit 1
fi

property=$1

grep $property app.config | cut -d '=' -f 2
