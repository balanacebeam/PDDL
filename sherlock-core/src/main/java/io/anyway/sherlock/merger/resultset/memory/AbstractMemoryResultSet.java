package io.anyway.sherlock.merger.resultset.memory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.anyway.sherlock.merger.resultset.memory.row.ResultSetRow;
import io.anyway.sherlock.merger.util.ResultSetUtil;
import io.anyway.sherlock.util.SQLUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

/**
 * 内存结果集抽象类.
 * 
 * @author xiong.j
 */
@Slf4j
public abstract class AbstractMemoryResultSet extends AbstractUnsupportedOperationMemoryResultSet {
    
    private boolean beforeFirst = true;
    
    @Getter(AccessLevel.PROTECTED)
    private ResultSetRow currentRow;
    
    private boolean wasNullFlag = false;
    
    public AbstractMemoryResultSet(final List<ResultSet> resultSets) throws SQLException {
        super(resultSets);
    }
    
    @Override
    public boolean next() throws SQLException {
        if (beforeFirst) {
            initRows(getResultSets());
            beforeFirst = false;
        }
        Optional<? extends ResultSetRow> row = nextRow();
        if (row.isPresent()) {
            currentRow = row.get();
            log.trace("Current row is {}", currentRow);
            return true;
        }
        return false;
    }
    
    protected abstract void initRows(final List<ResultSet> resultSets) throws SQLException;
    
    protected abstract Optional<? extends ResultSetRow> nextRow() throws SQLException;
    
    @Override
    public boolean wasNull() throws SQLException {
        return wasNullFlag;
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        String formattedColumnLabel = getColumnLabelIndexMap().containsKey(columnLabel) ? columnLabel : SQLUtil.getExactlyValue(columnLabel);
        if (!getColumnLabelIndexMap().containsKey(formattedColumnLabel)) {
            throw new SQLException(String.format("Column label %s does not exist", formattedColumnLabel));
        }
        return getColumnLabelIndexMap().get(formattedColumnLabel);
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        Preconditions.checkState(!isClosed(), "Result set is closed");
        Preconditions.checkState(!beforeFirst, "Before start of result set");
        Preconditions.checkState(null != currentRow, "After end of result set");
        Preconditions.checkArgument(currentRow.inRange(columnIndex), String.format("Column Index %d out of range", columnIndex));
        Object result = currentRow.getCell(columnIndex);
        wasNullFlag = null == result;
        return result;
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return getObject(findColumn(columnLabel));
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return (String) ResultSetUtil.convertValue(getObject(columnIndex), String.class);
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        return getString(findColumn(columnLabel));
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return false;
        }
        return (cell instanceof Boolean) ? (Boolean) cell : Boolean.valueOf(cell.toString());
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return getBoolean(findColumn(columnLabel));
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return 0;
        }
        return (Byte) ResultSetUtil.convertValue(cell, byte.class);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return getByte(findColumn(columnLabel));
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return 0;
        }
        return (Short) ResultSetUtil.convertValue(cell, short.class);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return getShort(findColumn(columnLabel));
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return 0;
        }
        return (Integer) ResultSetUtil.convertValue(cell, int.class);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return getInt(findColumn(columnLabel));
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return 0;
        }
        return (Long) ResultSetUtil.convertValue(cell, long.class);
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return getLong(findColumn(columnLabel));
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return 0;
        }
        return (Float) ResultSetUtil.convertValue(cell, float.class);
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return getFloat(findColumn(columnLabel));
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return 0;
        }
        return (Double) ResultSetUtil.convertValue(cell, double.class);
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return getDouble(findColumn(columnLabel));
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return null;
        }
        BigDecimal result = (BigDecimal) ResultSetUtil.convertValue(cell, BigDecimal.class);
        return result.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return getBigDecimal(findColumn(columnLabel), scale);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return null;
        }
        return (BigDecimal) ResultSetUtil.convertValue(cell, BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return getBigDecimal(findColumn(columnLabel));
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        String value = getString(columnIndex);
        if (wasNullFlag) {
            return null;
        }
        return value.getBytes();
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return getBytes(findColumn(columnLabel));
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return getDate(columnIndex, null);
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return getDate(findColumn(columnLabel));
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        // TODO 时间相关取值未实现calendar模式
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return null;
        }
        return (Date) ResultSetUtil.convertValue(cell, Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return getDate(findColumn(columnLabel), cal);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return getTime(columnIndex, null);
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return getTime(findColumn(columnLabel));
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return null;
        }
        return (Time) ResultSetUtil.convertValue(cell, Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return getTime(findColumn(columnLabel), cal);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return getTimestamp(columnIndex, null);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return getTimestamp(findColumn(columnLabel));
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        Object cell = getObject(columnIndex);
        if (wasNullFlag) {
            return null;
        }
        return (Timestamp) ResultSetUtil.convertValue(cell, Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return getTimestamp(findColumn(columnLabel), cal);
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        String value = getString(columnIndex);
        if (wasNullFlag) {
            return null;
        }
        try {
            return new URL(value);
        } catch (final MalformedURLException ex) {
            throw new SQLException("URL Malformed URL exception");
        }
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return getURL(findColumn(columnLabel));
    }
    
    @Override
    public Statement getStatement() throws SQLException {
        return getResultSets().get(0).getStatement();
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return getResultSets().get(0).getMetaData();
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        int result = 0;
        for (ResultSet each : getResultSets()) {
            result += each.getFetchSize();
        }
        return result;
    }
    
    @Override
    public int getType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
    }
}
