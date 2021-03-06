import React from 'react';
import { render } from 'test/utils';

import { Base } from './RecordingSnippet.stories';

describe('<RecordingSnippet />', () => {
  it('Should render snippet fot organization', async () => {
    const { queryByText, findByText } = render(<Base />);
    expect(queryByText('Loading')).toBeInTheDocument();

    await findByText(`._i_org = 'FE2Dj3';`, { exact: false });
    await findByText(`.src = 'https://static.rebrowse.dev/s/rebrowse.js';`, {
      exact: false,
    });
  });
});
