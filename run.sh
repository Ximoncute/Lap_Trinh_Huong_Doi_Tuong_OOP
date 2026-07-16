#!/bin/bash

echo "==================================================="
echo "     Dang khoi dong MoneyMate Premium App..."
echo "==================================================="

./maven/bin/mvn compile exec:java

if [ $? -ne 0 ]; then
    echo
    echo "[ERROR] Ung dung gap loi khi khoi chay."
    read -p "Nhan Enter de tiep tuc..."
fi