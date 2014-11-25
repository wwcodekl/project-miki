## Create a Game board

## define how many iterations we want to run

## apply the rules for as many iterations as we want
## For each cell, calculate the number of neighbours and determine whether they should live or die
    # ## RULES ####
    #
    # Any live cell with less than two live neighbours dies, as if caused by under-population.
    # Any live cell with two or three live neighbours lives on to the next generation.
    # Any live cell with more than three live neighbours dies, as if by overcrowding.
    # Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.

## each time, display the board


current_board = [(0, 0, 0, 0, 0),
                 (0, 0, 1, 0, 0),
                 (0, 0, 1, 0, 0),
                 (0, 0, 1, 0, 0),
                 (0, 0, 0, 0, 0)
]

maximum_iterations = 5

def make_line(row):
    line = ""
    return line


def print_board():
    return ""

def is_location_legal(row,column):
    return True

def count_neighbours(cell_row, cell_column):
    ## Who are the potential neighbours
    neighbours = []
    active_neighbors = 0

    ## Check each neighbour and then check if the cell is live
    for row,column in neighbours:
        if (is_location_legal(row,column)):
            if(current_board[row][column] == 1):
                ## do nothing
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
            
            # Any live cell with two or three live neighbours lives on to the next generation.
            # Any live cell with more than three live neighbours dies, as if by overcrowding.
            # Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
            current_cell = current_cell + 1
        current_row = current_row + 1
        new_board.append(new_row)

    return new_board


print_board()
for iteration in range(maximum_iterations):
    print "Iteration number " + str(iteration)
    current_board = update_board()
    print_board()

