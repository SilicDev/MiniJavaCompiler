int number, divisor;
boolean prim;

number = readInt();
divisor = 2;
prim = true;

while(divisor * divisor <= number && prim) {
    if(number % divisor == 0) {
        prim = false;
    }
    divisor = divisor + 1;
}
if(number <= 1) {
    prim = false;
}
write(prim);
