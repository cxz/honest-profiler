package com.insightfullogic.honest_profiler.ports.javafx.controller;

import static com.insightfullogic.honest_profiler.ports.javafx.util.DialogUtil.FILTER;
import static com.insightfullogic.honest_profiler.ports.javafx.util.ResourceUtil.TITLE_DIALOG_SPECIFYFILTERS;
import static com.insightfullogic.honest_profiler.ports.javafx.view.Icon.FUNNEL_16;
import static com.insightfullogic.honest_profiler.ports.javafx.view.Icon.FUNNEL_ACTIVE_16;
import static com.insightfullogic.honest_profiler.ports.javafx.view.Icon.viewFor;
import static javafx.scene.input.KeyCode.ENTER;

import java.util.ArrayList;
import java.util.List;

import com.insightfullogic.honest_profiler.core.filters.Filter;
import com.insightfullogic.honest_profiler.core.filters.ProfileFilter;
import com.insightfullogic.honest_profiler.core.filters.StringFilter;
import com.insightfullogic.honest_profiler.core.profiles.Profile;
import com.insightfullogic.honest_profiler.ports.javafx.controller.filter.FilterDialogController;
import com.insightfullogic.honest_profiler.ports.javafx.model.ApplicationContext;
import com.insightfullogic.honest_profiler.ports.javafx.model.filter.FilterSpecification;
import com.insightfullogic.honest_profiler.ports.javafx.model.filter.FilterType;
import com.insightfullogic.honest_profiler.ports.javafx.util.DialogUtil;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

/**
 * Superclass for all View Controllers in the application. These controllers provide a particular view on data. The
 * class holds the code for the filters and quick filter.
 */
public abstract class AbstractViewController extends AbstractController
{
    private Button filterButton;
    private Button quickFilterButton;
    private TextField quickFilterText;

    private FilterDialogController dialogController;
    private ObjectProperty<FilterSpecification> filterSpec = new SimpleObjectProperty<>(
        new FilterSpecification());

    private ProfileFilter currentFilter = new ProfileFilter();
    private StringFilter quickFilter;

    /**
     * This method must be called by subclasses in their FXML initialize(). It provides the controller-local UI nodes
     * needed by the AbstractViewController.
     *
     * @param filterButton the button used to trigger filter editing
     * @param quickFilterButton the button used to apply the quick filter
     * @param quickFilterText the TextField providing the value for the quick filter
     */
    protected void initialize(Button filterButton, Button quickFilterButton,
        TextField quickFilterText)
    {
        super.initialize();

        if (filterButton == null)
        {
            return;
        }

        this.filterButton = filterButton;
        this.quickFilterButton = quickFilterButton;
        this.quickFilterText = quickFilterText;
    }

    /**
     * In addition to the normal functionality, the method calls filter initialization, which needs the
     * ApplicationContext to be present. If a particular view controller
     *
     * @param applicationContext the ApplicationContext of this application
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        super.setApplicationContext(applicationContext);

        if (filterButton == null)
        {
            return;
        }

        initializeFilters(applicationContext);
    }

    /**
     * View Controllers must implement this, and return the {@link FilterType}s which are supported by them.
     *
     * @return an array containing the {@link FilterType}s supported by the view controller
     */
    protected abstract FilterType[] getAllowedFilterTypes();

    /**
     * Refreshes the view. The view should be updated based on the current state of the {@link Profile} and
     * {@link ProfileFilter}.
     */
    protected abstract void refresh();

    /**
     * Returns the current {@link FilterSpecification}.
     *
     * @return the current {@link FilterSpecification}
     */
    protected ObjectProperty<FilterSpecification> getFilterSpecification()
    {
        return filterSpec;
    }

    /**
     * Returns the currently active {@link ProfileFilter}, constructed from the current filters and the quick filter.
     *
     * @return the currently active {@link ProfileFilter}
     */
    protected ProfileFilter getAdjustedProfileFilter()
    {
        if (quickFilter == null)
        {
            return currentFilter;
        }

        List<Filter> filters = new ArrayList<>();
        filters.add(quickFilter);
        filters.addAll(currentFilter.getFilters());
        return new ProfileFilter(filters);
    }

    /**
     * Initializes the filters.
     *
     * @param applicationContext the {@link ApplicationContext}. The parameter is used to explicitly point out the
     *            dependency on the presense of the context.
     */
    private void initializeFilters(ApplicationContext applicationContext)
    {
        dialogController = createFilterDialog();
        dialogController.setApplicationContext(applicationContext);
        dialogController.addAllowedFilterTypes(getAllowedFilterTypes());

        filterSpec.addListener((property, oldValue, newValue) ->
        {
            filterButton.setGraphic(iconFor(newValue));
            currentFilter = new ProfileFilter(newValue.getFilters());
            refresh();
        });

        filterButton.setOnAction(event -> filterSpec.set(dialogController.showAndWait().get()));

        quickFilterButton.setOnAction(event -> applyQuickFilter());
        quickFilterText.setOnKeyPressed(event ->
        {
            if (event.getCode() == ENTER)
            {
                applyQuickFilter();
            }
        });
    }

    private FilterDialogController createFilterDialog()
    {
        return (FilterDialogController) DialogUtil
            .<FilterSpecification>newDialog(FILTER, getText(TITLE_DIALOG_SPECIFYFILTERS), false);
    }

    private ImageView iconFor(FilterSpecification spec)
    {
        return spec == null || !spec.isFiltering() ? viewFor(FUNNEL_16) : viewFor(FUNNEL_ACTIVE_16);
    }

    private void applyQuickFilter()
    {
        String input = quickFilterText.getText();
        quickFilter = input.isEmpty() ? null : new StringFilter(
            Filter.Mode.CONTAINS,
            frame -> frame.getFullName(),
            input);
        refresh();
    }
}
