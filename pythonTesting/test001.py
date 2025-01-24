# Helper function to represent cons in Python
def cons(left, right):
    return (left, right)

# Function to simulate addition using binary trees
# def add(Op1, Op2):
#     Result = Op1  # Initialize Result with the first operand
#     while Op2 is not None:  # Loop until Op2 (the second operand) becomes None
#         Result = cons(None, Result)  # Add one to the result by appending a cons
#         Op2 = Op2[1]  # Move to the tail of Op2 (simulate decrementing Op2)
#     return Result

# Function to pretty-print a binary tree as an integer
def tree_to_int(tree):
    count = 0
    while tree is not None:
        count += 1
        tree = tree[1]
    return count

def f001():
    t3 = (None, None)
    t4 = (None, None)
    t5 = (None, t4)
    t6 = (None, t5)
    t7 = ('int', t6)
    For = t7
    return tree_to_int(For)

print(f001())

Result = (None, (None, (None, (None, None))))  # 3 in binary tree form

print("Result" ,len(Result))

# Convert tree to string
def tree_to_string(tree):
# Initialize an empty result string
    result = ''
    
    # Iterate through each element in the data
    for element in tree:
        if element is None:
            continue  # Skip None values
        elif isinstance(element, tuple):
            result += tree_to_string(element)  # Recursive call for nested tuple
        else:
            result += str(element)  # Append the string element to result
    
    return result

# Example usage
tree = cons('a', cons(None, cons('c', 'd')))  # Representing "abc"
print(tree_to_string(tree))  # Output: "abc"

# def tree_to_string

# Example usage
print(tree_to_int(Result))  # Output: 3


def main():
    t0 = cons(None, None)
    t1 = cons("un", None)
    B = tree_to_string(t1)
    t2 = cons("un", "deu")
    # t3 = cons("deu", "trei")
    # t4 = cons("un", t3)
    # D = tree_to_string(t4)
    # return B, C, D

print(main())