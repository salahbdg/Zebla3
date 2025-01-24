# Define the CONS function that creates a pair (a list with two elements)
def CONS(a, b):
    return (a, b)

# Define the ADD function that concatenates two strings
def tree_to_default(tree):
    result = ''
    for element in tree:
        if element is None:
            continue
        elif isinstance(element, tuple):
            result += tree_to_default(element)
        else:
            result += str(element)
    return result
def main():
    t0 = CONS(None, None)
    A = t0
    t1 = CONS("un", None)
    B = t1
    t2 = CONS("un", "deu")
    C = t2
    t3 = CONS("deu", "trei")
    t4 = CONS("un", t3)
    D = t4
    return tree_to_default(B), tree_to_default(C), tree_to_default(D)

# Calling main and printing the result
result = main()
print("For:", result)
