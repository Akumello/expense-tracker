package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.*;
import java.text.*;
import java.io.*;

public class ExpenseList
{
    // The singleton of expense list
    private static ExpenseList expenseList = null;

    // list is the main underlying data structure for expense list
    private static ObservableList<Expense> list = null;
    // filteredList is a subset of list with only the Expenses the user is interested in
    private static FilteredList<Expense> filteredList = null;
    private boolean isFiltered;

    private ExpenseList()
    {
        list = FXCollections.observableArrayList();
        filteredList = null;
        isFiltered = false;
    }

    // There can be only one active expense list in the program
    // This will be implemented as a singleton
    public static ExpenseList getExpenseList()
    {
        if(expenseList == null)
            expenseList = new ExpenseList();

        return expenseList;
    }

    public ObservableList<Expense> getList() {
        return list;
    }
    public static void setList(ObservableList<Expense> list) {
        ExpenseList.list = list;
    }

    public ObservableList<Expense> getFilteredList() {
        return filteredList;
    }


    public int getSize() {
        if(isFiltered)
            return filteredList.size();
        else
            return list.size();
    }

    public boolean addExpense(Expense e) {
        return list.add(e);
    }

    public Expense getExpense(int index) {
        return this.getList().get(index);
    }

    public boolean removeExpense(Expense e) {
        return list.remove(e);
    }

    public void clear()
    {
        if(list != null)
            list.clear();
        if(filteredList != null) {
            filteredList = null;
            isFiltered = false;
        }
    }

    public void clearFilter()
    {
        filteredList = null;
        isFiltered = false;
    }

    @Override
    public String toString() {
        String retS = "{ ";
        for (int i = 0; i < this.getSize(); i++) {
            retS += "" + this.getList().get(i).toString();
            if (i != this.getSize() - 1) {
                retS += ", ";
            }
        }
        retS += " }";
        return retS;
    }

    public void filterByName(String name) {
        filteredList = new FilteredList<>(list, p -> true);
        filteredList.setPredicate(expense -> {
            if(expense.getName().contains(name))
                return true;
            else
                return false;
        });
        isFiltered = true;
    }

    public void filterByCategory(String category) {
        filteredList = new FilteredList<>(list, p -> true);
        filteredList.setPredicate(expense -> {
            if(expense.getCategory().equals(category))
                return true;
            else
                return false;
        });
        isFiltered = true;
    }


    public void filterByDate(Date start, Date end) {
        filteredList = new FilteredList<>(list, p -> true);
        filteredList.setPredicate(expense -> {
            if(expense.getDate().after(start) && expense.getDate().before(end))
                return true;
            else
                return false;
        });
        isFiltered = true;
    }

    public void filterByCost(double min, double max) {
        filteredList = new FilteredList<>(list, p -> true);
        filteredList.setPredicate(expense -> {
            if(expense.getCost() >= min && expense.getCost() <= max)
                return true;
            else
                return false;
        });
        isFiltered = true;
    }

    public void filterByRecurring(boolean recurEnabled) {
        filteredList = new FilteredList<>(list, p -> true);
        filteredList.setPredicate(expense -> {
            // The user may wish to see only items that recur OR items that don't
            if(recurEnabled)
                return expense.isScheduled();
            else
                return !expense.isScheduled();
        });
        isFiltered = true;
    }

    /**
     * Calculates the total cost of the user's expenses.
     * @return The total dollar amount of expenses
     */
    public double calculateTotalExpenses() {
        double total_cost = 0.00; //initializes total cost as $0.00

        for (int i = 0; i < list.size(); i++) { //for each expense in the expense list
            total_cost += getExpense(i).getCost(); //add each amount to the total cost
        }

        return total_cost;
    }

    /**
     * Saves the user's data to an external file.
     *
     * @param fileName String representation of the file name
     */
    public void saveUserData(String fileName) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream(fileName + ".txt")); //writes data to a file

        pw.println(getSize());

        for (int i = 0; i < getSize(); i++) {
            pw.println(getExpense(i).isScheduled());
            pw.println(getExpense(i).getName()); //then prints its data
            pw.println(getExpense(i).getCost());
            pw.println(getExpense(i).getCategory());
            pw.println(getExpense(i).getDate());
            pw.println(getExpense(i).getNote());

            if(getExpense(i).isScheduled()){
                pw.println(getExpense(i).getFrequency());
                pw.println(getExpense(i).getStopDate());
            }
        }

        pw.close();
    }

    /**
     * Loads the user's data from an external file.
     */
    public void loadUserData(String user)
    {
        // Clear out the current list
        clear();

        // Set up reader to read from the user's file
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(user + ".txt"));
        } catch (Exception ex){}

        try
        {
            // Read the number of expenses in the file
            int expenseCount = Integer.valueOf(input.readLine());
            for (int i = 0; i < expenseCount; i++)
            {
                // Location for the data to come into
                Expense loadedExpense;

                // Used to help convert the date string to a date object
                DateFormat dateFormat = new SimpleDateFormat("E MMM dd hh:mm:ss ZZZ yyyy");

                // Bring each line in as the appropriate variable
                boolean recurring = Boolean.valueOf(input.readLine());
                String name = input.readLine();
                double cost = Double.valueOf(input.readLine());
                String category = input.readLine();
                Date date = dateFormat.parse(input.readLine());
                Date endDate = null;
                String note = input.readLine();

                long frequency = 0;
                if (recurring)
                {
                    // Bring each line in as the appropriate variable
                    frequency = Long.valueOf(input.readLine());
                    endDate = dateFormat.parse(input.readLine());

                    // Construct the object
                    loadedExpense = new Expense(name, cost, category, date, endDate, note, frequency);
                }
                else
                    loadedExpense = new Expense(name, cost, category, date, note);

                expenseList.addExpense(loadedExpense);
            }

            input.close();
        }
        catch(Exception ex){
            System.out.println("There was a problem reading the file");
        }
    }

    public void changeFromRecurring(int i){
        filteredList.get(i).setScheduled(false);
    }

    public boolean addToFiltered(Expense e){
        return filteredList.add(e);
    }

    //Takes end date as input
    public void updateScheduledExpenses(Date end){
        Expense hold, e;
        this.filterByRecurring(true);

        for(int i = 0; i < this.getFilteredList().size(); i++) {
            hold = this.getFilteredList().get(i);
            if(hold.needsUpdate(end)){
                //Need function that puts lets user confirm stuff
                e = new Expense(hold.getName(), hold.getCost(), hold.getCategory(), hold.getNextOccurrence(), hold.getStopDate(), hold.getNote(), hold.getFrequency());
                //setAddPaneFields(hold);
                this.addExpense(e);
                this.changeFromRecurring(i);
                updateScheduledExpenses(end);
            }
        }
    }

    private Comparator<Expense> compareDate = new Comparator<Expense>() {
        public int compare(Expense e1, Expense e2) {
            return e1.getDate().compareTo(e2.getDate());
        }
    };

    /*
    public void sortByAmount() {
        Collections.sort(list, compareAmount);
    }
    public void sortByDate() {
        Collections.sort(list, compareDate);
    }

    public void sortByDateR() {
        Collections.sort(list, compareDate.reversed());
    }
    public void sortByAmountR() {
        Collections.sort(list, compareAmount.reversed());
    }

    private Comparator<Expense> compareAmount = new Comparator<Expense>() {
        public int compare(Expense e1, Expense e2) {
            return (new Double(e1.getAmount()).compareTo(new Double(e2.getAmount())));
        }
    };

    public void sortByName() {
        Collections.sort(list, compareName);
    }

    public void sortByNameR() {
        Collections.sort(list, compareName.reversed());
    }

    private Comparator<Expense> compareName = new Comparator<Expense>() {
        public int compare(Expense e1, Expense e2) {
            return e1.getName().compareTo(e2.getName());
        }
    };

    public void fSortByName() {
        Collections.sort(filteredList, compareName);
    }

    public void fSortByDate() {
        Collections.sort(filteredList, compareAmount);
    }

    public void fSortByAmount() {
        Collections.sort(filteredList, compareAmount);
    }

    public void fSortByNameR() {
        Collections.sort(filteredList, compareName.reversed());
    }

    public void fSortByDateR() {
        Collections.sort(filteredList, compareAmount.reversed());
    }

    public void fSortByAmountR() {
        Collections.sort(filteredList, compareAmount.reversed());
    }



    public void addToBothLists(Expense e) {
        list.add(e);
        filteredList.add(e);
    }

  /*
  public void updateScheduledExpenses(){
        Expense e;
        this.filterByRecurring();
        for(int i = 0; i < filteredList.size(); i++){
            if(filteredList.get(i).needsUpdate()){
                e = new Expense(filteredList.get(i).getName(),filteredList.get(i).getAmount(),filteredList.get(i).getCategory(),filteredList.get(i).getDate(),filteredList.get(i).getNote(),filteredList.get(i).getFrequency());
                this.addToBothLists(e);
            }
        }
    }
    */
}
