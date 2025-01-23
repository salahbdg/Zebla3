class BinTree:

    def __init__(self, left=None, right=None, key="CONS") -> None:
        self.left = left
        self.right = right
        self.key = key

    def __str__(self) -> str:
        return self.pp()

    def copy(self):
        return BinTree(self.left, self.right, self.key)

    def pp(self) -> str:
        if self.key == "NIL":
            return "nil"
        elif self.key == "CONS":
            if self.left.key == "int":
                return str(self.right.toInt())
            elif self.left.key == "bool":
                return "true" if self.right.toBool() else "false"
            elif self.left.key == "string":
                return self.right.toString()
            else:
                return f"(cons {self.left.pp()} {self.right.pp()})"
        else:
            return self.key

    def toInt(self):
        value = 0
        cursor = self
        if cursor.key == "CONS":
            value += 1
        while cursor.right:
            cursor = cursor.right
            if cursor.key == "CONS":
                value += 1
        return value

    def toBool(self):
        value = False
        cursor = self
        if cursor.key == "CONS":
            value = True
        return value

    def toString(self):
        value = ""
        cursor = self

        if cursor.key == "CONS":
            if cursor.left:
                value += cursor.left.toString()
            if cursor.right:
                value += cursor.right.toString()
        else:
            if cursor.key != "NIL":
                value += cursor.key
        return f"{value}"


def main():
  global stack
  output = []
  A_0 = BinTree(key="NIL")
  B_1 = BinTree(key="NIL")
  C_2 = BinTree(key="NIL")
  D_3 = BinTree(key="NIL")
  # Assign
  CONS_FIRST_5 = BinTree(key="NIL")
  CONS_4 = CONS_FIRST_5.copy()
  A_0 = CONS_4.copy()
  # Assign
  SYMBOL_8 = BinTree(key= "un")
  CONS_FIRST_7 = SYMBOL_8.copy()
  CONS_6 = CONS_FIRST_7.copy()
  B_1 = CONS_6.copy()
  # Assign
  SYMBOL_11 = BinTree(key= "deu")
  CONS_FIRST_10 = SYMBOL_11.copy()
  SYMBOL_13 = BinTree(key= "un")
  CONS_12 = BinTree(left=SYMBOL_13.copy())
  CONS_12.right = CONS_FIRST_10.copy()
  CONS_9 = CONS_12.copy()
  C_2 = CONS_9.copy()
  # Assign
  SYMBOL_16 = BinTree(key= "trei")
  CONS_FIRST_15 = SYMBOL_16.copy()
  SYMBOL_18 = BinTree(key= "deu")
  CONS_17 = BinTree(left=SYMBOL_18.copy())
  CONS_17.right = CONS_FIRST_15.copy()
  SYMBOL_20 = BinTree(key= "un")
  CONS_19 = BinTree(left=SYMBOL_20.copy())
  CONS_19.right = CONS_17.copy()
  CONS_14 = CONS_19.copy()
  D_3 = CONS_14.copy()
  output.append(A_0)
  output.append(B_1)
  output.append(C_2)
  output.append(D_3)
  return output

if __name__ == "__main__":
    stack = []
    for i, value in enumerate(main()):
        print(value)
