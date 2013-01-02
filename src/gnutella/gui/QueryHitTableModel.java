package gnutella.gui;

import gnutella.utils.FileUtils;
import gnutella.utils.StringUtils;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class QueryHitTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 5998340986259873251L;

	// MD5ハッシュ値,ファイル名,共有者数
	public static final int COLUMN_COUNT = 3;

	public static final int COLUMN_FIELD_MD5 = 0;
	public static final int COLUMN_FIELD_FILE_NAME = 1;
	public static final int COLUMN_FIELD_NODE_COUNT = 2;

	private ArrayList<QueryHitTableBean> tabeleBeanList;
	public QueryHitTableModel(){
		this.tabeleBeanList = new ArrayList<QueryHitTableBean>();
	}
	
	@Override
	public int getRowCount() {
		return tabeleBeanList.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		QueryHitTableBean bean = this.tabeleBeanList.get(rowIndex);
		if(bean==null){
			return null;
		}
		switch (columnIndex) {
		case COLUMN_FIELD_MD5:
			return FileUtils.getDigestStringExpression(bean.getMD5Digest());
		case COLUMN_FIELD_FILE_NAME:
			return StringUtils.join(bean.getFileNames(),",");
		case COLUMN_FIELD_NODE_COUNT:
			return bean.getFileSharingNodeCount();
		default:
			break;
		}
		return null;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case COLUMN_FIELD_FILE_NAME:
			return "ファイル名";
		case COLUMN_FIELD_MD5:
			return "MD5";
		case COLUMN_FIELD_NODE_COUNT:
			return "共有者数";
		default:
			break;
		}
		return "";
	}
	
	public void addRow(QueryHitTableBean queryHitTableBean){
		this.tabeleBeanList.add(queryHitTableBean);
		fireTableRowsInserted(this.tabeleBeanList.size()-1,this.tabeleBeanList.size());
	}
	
	public void clearRow(){
		this.tabeleBeanList.clear();
		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex){
		return false;
	}
}

