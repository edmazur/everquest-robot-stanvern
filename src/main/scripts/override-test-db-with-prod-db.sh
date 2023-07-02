#!/bin/bash

database=$(mktemp)

mysqldump \
  --no-tablespaces \
  --add-drop-database \
  -h $(./src/main/scripts/get-config-value.sh mysql.host.prod) \
  -u $(./src/main/scripts/get-config-value.sh mysql.username.prod) \
  -p$(./src/main/scripts/get-config-value.sh mysql.password.prod) \
  --databases $(./src/main/scripts/get-config-value.sh mysql.database.prod) \
  > $database

mysql \
  -u $(./src/main/scripts/get-config-value.sh mysql.username.test) \
  -p$(./src/main/scripts/get-config-value.sh mysql.password.test) \
  < $database
