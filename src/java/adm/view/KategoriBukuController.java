package adm.view;

import adm.model.KategoriBuku;
import adm.view.util.JsfUtil;
import adm.view.util.PaginationHelper;
import adm.controller.KategoriBukuFacade;
import adm.model.Buku;

import java.io.Serializable;
import java.util.Random;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

/**
 *
 * @author RAKA
 */
@Named("kategoriBukuController")
@SessionScoped
public class KategoriBukuController implements Serializable {

    private KategoriBuku current;
    private DataModel items = null;
    @EJB
    private adm.controller.KategoriBukuFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private int id ;//= current.getIdKategori();

    /**
     *
     */
    public KategoriBukuController() {
    }

    /**
     *
     * @return
     */
    public KategoriBuku getSelected() {
        if (current == null) {
            current = new KategoriBuku();
            selectedItemIndex = -1;
        }
        return current;
    }

    private KategoriBukuFacade getFacade() {
        return ejbFacade;
    }

    /**
     *
     * @return
     */
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper() {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    /**
     *
     * @return
     */
    public String prepareList() {
        recreateModel();
        return "List";
    }

    /**
     *
     * @return
     */
    public String prepareView() {
        current = (KategoriBuku) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    /**
     *
     * @return
     */
    public String prepareCreate() {
        current = new KategoriBuku();
        selectedItemIndex = -1;
        return "Create";
    }
    
    public void autoInc(){
        Random rd = new Random();
        id = rd.nextInt(1000);
        current.setIdKategori(id);
    }

    /**
     *
     * @return
     */
    public String create() {
        try {
            autoInc();
            getFacade().create(current);
            JsfUtil.addSuccessMessage("Data Kategori Buku berhasil ditambahkan");
            recreateModel();
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString(e.toString()));
            return null;
        }
    }

    /**
     *
     * @return
     */
    public String prepareEdit() {
        current = (KategoriBuku) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    /**
     *
     * @return
     */
    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage("Data Anggota berhasil ditambahkan");
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    /**
     *
     * @return
     */
    public String destroy() {
        current = (KategoriBuku) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }
    
    /**
     *
     */
    public void ubahStatus(){
        current = (KategoriBuku) getItems().getRowData();
        if(current.getStatus()== 1){
            current.setStatus(0);
        }else if(current.getStatus()== 0){
            current.setStatus(1);
        }
    }

    /**
     *
     * @return
     */
    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("KategoriBukuDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    /**
     *
     * @return
     */
    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    /**
     *
     * @return
     */
    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    /**
     *
     * @return
     */
    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    /**
     *
     * @return
     */
    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    /**
     *
     * @return
     */
    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    /**
     *
     * @param id
     * @return
     */
    public KategoriBuku getKategoriBuku(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    /**
     *
     */
    @FacesConverter(forClass = KategoriBuku.class)
    public static class KategoriBukuControllerConverter implements Converter {

        /**
         *
         * @param facesContext
         * @param component
         * @param value
         * @return
         */
        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            KategoriBukuController controller = (KategoriBukuController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "kategoriBukuController");
            return controller.getKategoriBuku(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        /**
         *
         * @param facesContext
         * @param component
         * @param object
         * @return
         */
        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof KategoriBuku) {
                KategoriBuku o = (KategoriBuku) object;
                return getStringKey(o.getIdKategori());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + KategoriBuku.class.getName());
            }
        }

    }

}
