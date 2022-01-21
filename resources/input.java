block1 : {
    int in, op, res;

    res = readInt();
    op = readInt();

    while (0 < op) {
        block2: {
            if (op == 1) {
                in = readInt();
                op = res;
                while (0 < in) {
                   res = res * op;
                   in = in - 1;
                }
            }
        }
        block3 : {
            if (op == 2) {
                in = res;
                res = 0;
                while (0 < in) {
                    res = res + in;
                    in = in - 1;
                }
            }
        }
        block4 : {
            if (op == 3) {
                in = readInt();
                if (0 <= in) {
                    while (0 < in) {
                        res = res * 10;
                        in = in - 1;
                    }
                } else {
                    block5 : {
                        in = -in;
                        while (0 < in) {
                            res = res / 10;
                            in = in - 1;
                        }
                    }
                }
            }
        }
        block6 : {
            op = readInt();
        }
    }

    write(res);
}