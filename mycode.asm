start:
add $8, $17, $18
sub $9, $19, $20
and $10, $21, $22
or $11, $23, $24

label:
sll $12, $25, 2
srl $13, $26, 3
sllv $14, $22, $22
srlv $15, $29, $30

main:
addi $16, $2, 20
andi $11, $18, 55
lw $18, 100($19)
sw $19, -200($20)

branch:
blez $14,main
bgtz $2,label
beq $9,$14,start
bne $6,$7,start

j branch