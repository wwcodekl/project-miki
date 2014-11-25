
## Create a Game board
## define how many iterations we want to run

## apply the rules for as many iterations as we want

## each time, display the board

# Any live cell with less than two live neighbours dies, as if caused by under-population.
# Any live cell with two or three live neighbours lives on to the next generation.
# Any live cell with more than three live neighbours dies, as if by overcrowding.
# Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.

current_board = [(0, 0, 0, 0, 0),
                 (0, 0, 1, 0, 0),
                 (0, 0, 1, 0, 0),
                 (0, 0, 1, 0, 0),
                 (0, 0, 0, 0, 0)
]



def is_location_legal(row,column):
    if row < 0 or column < 0:
        return False

    if row >= 5 or column >= 5:
        return False

    if row >= 0 and column >= 5:
        return  False

    if row <= 5 and column < 0:
        return False

    if row <= 5 and column < 0:
        return False
    return True

def count_neighbours(cell_row, cell_column):
    ## what are the potential neighbours
    neighbours = [(cell_row - 1, cell_column - 1),
                  (cell_row - 1, cell_column),
                  (cell_row -1, cell_column +1),
                  (cell_row, cell_column + 1),
                  (cell_row, cell_column - 1),
                  (cell_row + 1,cell_column -1),
                  (cell_row + 1,cell_column),
                  (cell_row + 1,cell_column +1)
    ]
    active_neighbors = 0
    for row,column in neighbours:
        if (is_location_legal(row,column)):
            #look at the location
            #print "Accessing " + str(row) + " : " + str(column) + " Current board " + str(len(current_board[row]))
            if(current_board[row][column] == 1):
                active_neighbors = active_neighbors + 1

    return active_neighbors

def update_board():
    current_row = 0
    new_board = []
    ## calculate the update
    for row in current_board:
        current_cell = 0
        new_row = []
        for cell in row:
            neighbors = count_neighbours(current_row,current_cell)
            # Any live cell with less than two live neighbours dies, as if caused by under-population.
            if current_board[current_row][current_cell] == 1 and neighbors < 2:
                new_row.append(0)
            # Any live cell with two or three live neighbours lives on to the next generation.
            elif current_board[current_row][current_cell] == 1 and neighbors in [2,3]:
                new_row.append(1)
            # Any live cell with more than three live neighbours dies, as if by overcrowding.
            elif current_board[current_row][current_cell] == 1 and neighbors > 3:
                new_row.append(0)
            # Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
            elif current_board[current_row][current_cell] == 0 and neighbors == 3:
                new_row.append(1)
            else:
                new_row.append(0)
            current_cell = current_cell + 1

        current_row = current_row + 1

        new_board.append(new_row)

    return new_board

def make_line(row):
    line = ""
    for cell in row:
        if(cell == 0):
            line = line + "-"
        else:
            line = line + "*"
    return line


def print_board():
    for row in current_board:
        print make_line(row)


maximum_iterations = 5
print_board()
for iteration in range(maximum_iterations):
    print "Iteration number " + str(iteration)
    current_board = update_board()
    print_board()

