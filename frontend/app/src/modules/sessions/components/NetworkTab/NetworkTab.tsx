import React from 'react';
import { BrowserFetchEventDTO } from '@insight/types';
import { Block } from 'baseui/block';
import { StyledSpinnerNext } from 'baseui/spinner';
import { useStyletron } from 'baseui';
import {
  Unstable_StatefulDataTable as DataTable,
  StringColumn,
  NumericalColumn,
  CategoricalColumn,
} from 'baseui/data-table';

type Props = {
  loading: boolean;
  events: BrowserFetchEventDTO[];
};

const NetworkTab = ({ events, loading }: Props) => {
  const [_css, theme] = useStyletron();

  const columns = [
    CategoricalColumn({
      title: 'Method',
      mapDataToValue: (data: BrowserFetchEventDTO) => data.method,
    }),
    NumericalColumn({
      title: 'Status',
      mapDataToValue: (data: BrowserFetchEventDTO) => data.status,
    }),
    CategoricalColumn({
      title: 'Type',
      mapDataToValue: (data: BrowserFetchEventDTO) => data.type,
    }),
    StringColumn({
      title: 'Name',
      mapDataToValue: (data: BrowserFetchEventDTO) => {
        let pathname;
        let search;
        if (data.url[0] === '/') {
          pathname = data.url;
          search = `?${pathname.split('?')[1]}`;
        } else {
          const url = new URL(data.url);
          pathname = url.pathname;
          search = url.search;
        }
        const split = pathname.split('/');
        return `${split[split.length - 1]}${search}`;
      },
    }),
  ];

  const rows = events.map((e) => ({ data: e, id: e.url }));

  return (
    <Block
      overflow="auto"
      width="100%"
      height={!loading ? `${50 * rows.length + 110}px` : undefined}
    >
      {loading ? (
        <Block display="flex" justifyContent="center">
          <StyledSpinnerNext $style={{ marginTop: theme.sizing.scale500 }} />
        </Block>
      ) : (
        <DataTable
          columns={columns}
          rows={rows}
          $style={{ width: '100%', overflow: 'auto' }}
          rowHeight={50}
        />
      )}
    </Block>
  );
};

export default React.memo(NetworkTab);